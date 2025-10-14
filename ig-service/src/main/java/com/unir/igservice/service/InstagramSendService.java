package com.unir.igservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unir.common.entity.Account;
import com.unir.common.entity.Message;
import com.unir.common.entity.Thread;
import com.unir.common.model.Channel;
import com.unir.common.model.Direction;
import com.unir.igservice.config.InstagramConfig;
import com.unir.igservice.dto.InstagramSendRequest;
import com.unir.igservice.dto.InstagramSendResponse;
import com.unir.igservice.repository.AccountRepository;
import com.unir.igservice.repository.MessageRepository;
import com.unir.igservice.repository.ThreadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class InstagramSendService {
    
    private static final Logger log = LoggerFactory.getLogger(InstagramSendService.class);
    
    private final WebClient instagramWebClient;
    private final InstagramConfig config;
    private final AccountRepository accountRepository;
    private final ThreadRepository threadRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public InstagramSendService(
            @Qualifier("instagramWebClient") WebClient instagramWebClient,
            InstagramConfig config,
            AccountRepository accountRepository,
            ThreadRepository threadRepository,
            MessageRepository messageRepository,
            ObjectMapper objectMapper) {
        this.instagramWebClient = instagramWebClient;
        this.config = config;
        this.accountRepository = accountRepository;
        this.threadRepository = threadRepository;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public InstagramSendResponse sendMessage(InstagramSendRequest request) {
        log.info("Enviando mensaje de Instagram desde account {} a {}", request.getAccountId(), request.getToId());
        
        // 1. Obtener la cuenta y sus credenciales
        UUID accountId = request.getAccountId();
        if (accountId == null) {
            throw new RuntimeException("Account ID is required");
        }
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
        
        // 2. Extraer access_token (TODO: descifrar con CryptoUtil en producción)
        String accessToken = extractAccessToken(account);
        String igUserId = account.getExternalIds().get("ig_user_id");
        
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("Access token no disponible para account: {}", accountId);
            throw new RuntimeException("Access token not configured");
        }
        
        if (igUserId == null || igUserId.isEmpty()) {
            log.warn("ig_user_id no encontrado, usando configuración por defecto");
            igUserId = config.getUserId();
        }
        
        // 3. Enviar mensaje a Instagram Graph API
        String uri = String.format("/%s/%s/messages", config.getGraphVersion(), igUserId);
        
        Map<String, Object> body = new HashMap<>();
        body.put("recipient", Map.of("id", request.getToId()));
        body.put("message", Map.of("text", request.getText()));

        try {
            Map<String, Object> response = instagramWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri)
                            .queryParam("access_token", accessToken)
                            .build())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .block();

            String messageId = response != null ? (String) response.get("message_id") : null;
            String recipientId = response != null ? (String) response.get("recipient_id") : request.getToId();
            
            log.info("Mensaje enviado exitosamente a Instagram. Message ID: {}", messageId);
            
            // 4. Persistir mensaje outbound en la DB
            persistOutboundMessage(account, request.getToId(), request.getText(), messageId);
            
            return new InstagramSendResponse(messageId, "sent");
            
        } catch (Exception e) {
            log.error("Error al enviar mensaje a Instagram Graph API", e);
            throw new RuntimeException("Failed to send Instagram message: " + e.getMessage(), e);
        }
    }
    
    private String extractAccessToken(Account account) {
        try {
            String credentialsJson = account.getCredentialsEncrypted();
            if (credentialsJson == null) {
                return config.getPageAccessToken(); // Fallback
            }
            
            // TODO: En producción, usar CryptoUtil.decrypt(credentialsJson)
            Map<String, String> credentials = objectMapper.readValue(credentialsJson, Map.class);
            return credentials.get("access_token");
        } catch (Exception e) {
            log.warn("Error extrayendo access token, usando configuración: {}", e.getMessage());
            return config.getPageAccessToken();
        }
    }
    
    private void persistOutboundMessage(Account account, String recipientId, String text, String messageId) {
        try {
            // Buscar o crear thread
            String externalThreadId = recipientId; // En Instagram, usamos el recipient ID como thread ID
            Thread thread = threadRepository.findByAccountIdAndExternalThreadId(account.getId(), externalThreadId)
                    .orElseGet(() -> {
                        Thread newThread = new Thread();
                        newThread.setAccountId(account.getId());
                        newThread.setChannel(Channel.INSTAGRAM);
                        newThread.setExternalThreadId(externalThreadId);
                        
                        Thread.Participant participant = new Thread.Participant(recipientId, "Recipient");
                        newThread.setParticipants(List.of(participant));
                        newThread.setLastMessageAt(OffsetDateTime.now());
                        
                        return threadRepository.save(newThread);
                    });
            
            // Crear mensaje outbound
            Message message = new Message();
            message.setThreadId(thread.getId());
            message.setChannel(Channel.INSTAGRAM);
            message.setDirection(Direction.OUTBOUND);
            message.setExternalMessageId(messageId);
            
            Map<String, String> sender = new HashMap<>();
            sender.put("id", account.getExternalIds().get("ig_user_id"));
            sender.put("name", account.getDisplayName() != null ? account.getDisplayName() : "Me");
            message.setSender(sender);
            
            List<Map<String, String>> recipients = new ArrayList<>();
            Map<String, String> recipient = new HashMap<>();
            recipient.put("id", recipientId);
            recipient.put("name", "Recipient");
            recipients.add(recipient);
            message.setRecipients(recipients);
            
            message.setBodyText(text);
            message.setStatus("sent");
            message.setCreatedAt(OffsetDateTime.now());
            
            messageRepository.save(message);
            
            // Actualizar thread
            thread.setLastMessageAt(message.getCreatedAt());
            threadRepository.save(thread);
            
            log.info("Mensaje outbound persistido: {} en thread {}", message.getId(), thread.getId());
            
        } catch (Exception e) {
            log.error("Error persistiendo mensaje outbound (no crítico)", e);
            // No lanzamos excepción porque el mensaje ya se envió exitosamente a Instagram
        }
    }
}

