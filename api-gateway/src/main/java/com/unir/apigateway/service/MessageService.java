package com.unir.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unir.apigateway.dto.MessageDTO;
import com.unir.apigateway.repository.MessageRepository;
import com.unir.common.dto.SendMessageRequest;
import com.unir.common.entity.Message;
import com.unir.common.model.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageService {
    
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    
    private final MessageRepository messageRepository;
    private final WebClient igServiceWebClient;
    private final SseService sseService;
    private final ObjectMapper objectMapper;
    
    @Value("${services.ig-service.url}")
    private String igServiceUrl;

    public MessageService(
            MessageRepository messageRepository,
            @Qualifier("igServiceWebClient") WebClient igServiceWebClient,
            SseService sseService,
            ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.igServiceWebClient = igServiceWebClient;
        this.sseService = sseService;
        this.objectMapper = objectMapper;
    }

    public Page<MessageDTO> listMessages(UUID threadId, Pageable pageable) {
        Page<Message> messages = messageRepository.findByThreadIdOrderByCreatedAtDesc(threadId, pageable);
        return messages.map(MessageDTO::fromEntity);
    }

    public MessageDTO sendMessage(SendMessageRequest request) {
        log.info("Enviando mensaje a canal: {} desde account: {}", request.getChannel(), request.getAccountId());
        
        // Validaciones
        if (request.getTo() == null || request.getTo().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar al menos un destinatario");
        }
        
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("El texto del mensaje no puede estar vacío");
        }
        
        // Routing basado en el canal
        if (request.getChannel() == Channel.INSTAGRAM) {
            return sendInstagramMessage(request);
        } else if (request.getChannel() == Channel.WHATSAPP) {
            throw new UnsupportedOperationException("WhatsApp no implementado aún");
        } else if (request.getChannel() == Channel.GMAIL) {
            throw new UnsupportedOperationException("Gmail no implementado aún");
        } else {
            throw new UnsupportedOperationException("Canal no soportado: " + request.getChannel());
        }
    }
    
    private MessageDTO sendInstagramMessage(SendMessageRequest request) {
        // Preparar request para ig-service
        Map<String, Object> serviceRequest = new HashMap<>();
        serviceRequest.put("accountId", request.getAccountId().toString());
        serviceRequest.put("toId", request.getTo().get(0).getId());
        serviceRequest.put("text", request.getText());

        try {
            // Enviar a ig-service
            Map<String, Object> response = igServiceWebClient.post()
                    .uri("/v1/ig/send")
                    .bodyValue(serviceRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Mensaje de Instagram enviado exitosamente: {}", response);
            
            String messageId = (String) response.get("messageId");
            String status = (String) response.get("status");
            
            // Emitir evento SSE para notificar al frontend en tiempo real
            try {
                String eventData = objectMapper.writeValueAsString(response);
                sseService.emitEvent("message.sent", eventData);
            } catch (Exception e) {
                log.warn("Error emitiendo evento SSE: {}", e.getMessage());
            }
            
            // Construir DTO de respuesta
            MessageDTO dto = new MessageDTO();
            dto.setId(UUID.randomUUID()); // Nota: el mensaje real se persiste en ig-service
            dto.setChannel(request.getChannel());
            dto.setBodyText(request.getText());
            dto.setStatus(status != null ? status : "sent");
            dto.setExternalMessageId(messageId);
            
            return dto;
            
        } catch (Exception e) {
            log.error("Error enviando mensaje de Instagram", e);
            throw new RuntimeException("Failed to send Instagram message: " + e.getMessage(), e);
        }
    }
}

