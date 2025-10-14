package com.unir.apigateway.controller;

import com.unir.apigateway.dto.MessageDTO;
import com.unir.apigateway.service.MessageService;
import com.unir.common.dto.SendMessageRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class MessageController {
    
    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Listar mensajes de un thread específico
     * GET /v1/threads/{threadId}/messages
     */
    @GetMapping("/threads/{threadId}/messages")
    public ResponseEntity<Page<MessageDTO>> getMessagesByThreadId(
            @PathVariable UUID threadId,
            Pageable pageable) {
        log.debug("Listando mensajes del thread: {}", threadId);
        Page<MessageDTO> messages = messageService.listMessages(threadId, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * Enviar un mensaje
     * POST /v1/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        log.info("Recibiendo request para enviar mensaje: channel={}, accountId={}", 
                 request.getChannel(), request.getAccountId());
        
        try {
            MessageDTO response = messageService.sendMessage(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validación falló: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (UnsupportedOperationException e) {
            log.warn("Operación no soportada: {}", e.getMessage());
            return ResponseEntity.status(501).build(); // Not Implemented
        } catch (Exception e) {
            log.error("Error enviando mensaje", e);
            return ResponseEntity.status(500).build();
        }
    }
}
