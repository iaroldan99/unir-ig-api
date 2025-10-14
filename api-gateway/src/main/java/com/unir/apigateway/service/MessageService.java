package com.unir.apigateway.service;

import com.unir.apigateway.dto.MessageDTO;
import com.unir.apigateway.repository.MessageRepository;
import com.unir.common.dto.SendMessageRequest;
import com.unir.common.entity.Message;
import com.unir.common.model.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final WebClient webClient;
    private final SseService sseService;
    
    @Value("${services.ig-service.url}")
    private String igServiceUrl;

    public MessageService(MessageRepository messageRepository, WebClient webClient, SseService sseService) {
        this.messageRepository = messageRepository;
        this.webClient = webClient;
        this.sseService = sseService;
    }

    public Page<MessageDTO> listMessages(UUID threadId, Pageable pageable) {
        Page<Message> messages = messageRepository.findByThreadIdOrderByCreatedAtDesc(threadId, pageable);
        return messages.map(MessageDTO::fromEntity);
    }

    public MessageDTO sendMessage(SendMessageRequest request) {
        log.info("Enviando mensaje a canal: {}", request.getChannel());
        
        // Routing basado en el canal
        String serviceUrl = switch (request.getChannel()) {
            case INSTAGRAM -> igServiceUrl + "/v1/ig/send";
            case WHATSAPP -> throw new UnsupportedOperationException("WhatsApp no implementado aún");
            case GMAIL -> throw new UnsupportedOperationException("Gmail no implementado aún");
        };

        // Preparar request para el servicio específico
        Map<String, Object> serviceRequest = new HashMap<>();
        serviceRequest.put("accountId", request.getAccountId().toString());
        
        if (!request.getTo().isEmpty()) {
            serviceRequest.put("toId", request.getTo().get(0).getId());
        }
        
        serviceRequest.put("text", request.getText());

        try {
            // Enviar al servicio correspondiente
            Map<String, Object> response = webClient.post()
                    .uri(serviceUrl)
                    .bodyValue(serviceRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Mensaje enviado exitosamente: {}", response);
            
            // Emitir evento SSE
            sseService.emitEvent("message.created", response);
            
            // Construir DTO de respuesta
            MessageDTO dto = new MessageDTO();
            dto.setId(UUID.randomUUID());
            dto.setChannel(request.getChannel());
            dto.setBodyText(request.getText());
            dto.setStatus("sent");
            
            return dto;
        } catch (Exception e) {
            log.error("Error enviando mensaje", e);
            throw new RuntimeException("Failed to send message", e);
        }
    }
}

