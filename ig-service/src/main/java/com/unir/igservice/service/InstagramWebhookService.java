package com.unir.igservice.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unir.common.entity.Message;
import com.unir.common.entity.Thread;
import com.unir.common.model.Channel;
import com.unir.common.model.Direction;
import com.unir.igservice.dto.InstagramWebhookPayload;
import com.unir.igservice.repository.AccountRepository;
import com.unir.igservice.repository.MessageRepository;
import com.unir.igservice.repository.ThreadRepository;

@Service
public class InstagramWebhookService {
    
    private static final Logger log = LoggerFactory.getLogger(InstagramWebhookService.class);
    
    private final ThreadRepository threadRepository;
    private final MessageRepository messageRepository;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    public InstagramWebhookService(ThreadRepository threadRepository, 
                                   MessageRepository messageRepository,
                                   AccountRepository accountRepository,
                                   ObjectMapper objectMapper) {
        this.threadRepository = threadRepository;
        this.messageRepository = messageRepository;
        this.accountRepository = accountRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processWebhook(InstagramWebhookPayload payload) {
        if (payload.getEntry() == null) {
            return;
        }

        for (InstagramWebhookPayload.Entry entry : payload.getEntry()) {
            if (entry.getMessaging() == null) {
                continue;
            }

            for (InstagramWebhookPayload.MessagingEvent event : entry.getMessaging()) {
                processMessagingEvent(event);
            }
        }
    }

    private void processMessagingEvent(InstagramWebhookPayload.MessagingEvent event) {
        if (event.getMessage() == null) {
            log.debug("Evento sin mensaje, saltando");
            return;
        }

        String senderId = event.getSender().getId();
        String recipientId = event.getRecipient().getId();
        String externalThreadId = senderId; // Usar sender como thread ID
        
        // TODO: Buscar account por recipientId (que debería matchear IG_USER_ID)
        // Por ahora usar accountId fijo o crear uno de prueba
        UUID accountId = findOrCreateAccountId(recipientId);
        
        // Buscar o crear thread
        Thread thread = threadRepository.findByAccountIdAndExternalThreadId(accountId, externalThreadId)
                .orElseGet(() -> {
                    Thread newThread = new Thread();
                    newThread.setAccountId(accountId);
                    newThread.setChannel(Channel.INSTAGRAM);
                    newThread.setExternalThreadId(externalThreadId);
                    
                    Thread.Participant participant = new Thread.Participant(senderId, "Instagram User");
                    newThread.setParticipants(List.of(participant));
                    newThread.setLastMessageAt(OffsetDateTime.now());
                    
                    log.info("Creando nuevo thread para sender: {}", senderId);
                    return threadRepository.save(newThread);
                });

        // Verificar si el mensaje ya existe (idempotencia)
        String externalMessageId = event.getMessage().getMid();
        Optional<Message> existing = messageRepository.findByThreadIdAndExternalMessageId(
                thread.getId(), externalMessageId);
        
        if (existing.isPresent()) {
            log.debug("Mensaje {} ya existe (idempotente), saltando", externalMessageId);
            return;
        }

        // Crear mensaje
        Message message = new Message();
        message.setThreadId(thread.getId());
        message.setChannel(Channel.INSTAGRAM);
        message.setDirection(Direction.INBOUND);
        message.setExternalMessageId(externalMessageId);
        
        Map<String, String> sender = new HashMap<>();
        sender.put("id", senderId);
        sender.put("name", "Instagram User");
        message.setSender(sender);
        
        // Recipients (el recipient del evento, típicamente nuestra app)
        List<Map<String, String>> recipients = new ArrayList<>();
        Map<String, String> recipient = new HashMap<>();
        recipient.put("id", recipientId);
        recipient.put("name", "Me");
        recipients.add(recipient);
        message.setRecipients(recipients);
        
        message.setBodyText(event.getMessage().getText());
        message.setStatus("received");
        
        // Guardar el payload completo en raw
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> rawPayload = objectMapper.convertValue(event, Map.class);
            message.setRaw(rawPayload);
        } catch (Exception e) {
            log.warn("Error convirtiendo evento a Map para raw: {}", e.getMessage());
        }
        
        if (event.getTimestamp() != null) {
            OffsetDateTime createdAt = OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(event.getTimestamp()), ZoneOffset.UTC);
            message.setCreatedAt(createdAt);
        }

        messageRepository.save(message);
        
        // Actualizar thread
        thread.setLastMessageAt(message.getCreatedAt());
        threadRepository.save(thread);
        
        log.info("Mensaje procesado: {} en thread {} (idempotente por external_message_id)", 
                 message.getId(), thread.getId());
    }
    
    /**
     * TODO: Implementar búsqueda real de account por recipientId (IG_USER_ID).
     * Por ahora retorna un UUID fijo para desarrollo/pruebas.
     */
    private UUID findOrCreateAccountId(String recipientId) {
        // En producción: buscar en accounts donde external_ids->>'ig_user_id' = recipientId
        // return accountRepository.findByChannelAndExternalIgUserId(Channel.INSTAGRAM, recipientId)
        //        .map(Account::getId)
        //        .orElse(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        
        // Por ahora, retornar UUID fijo
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}

