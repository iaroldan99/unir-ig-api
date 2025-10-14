package com.unir.apigateway.controller;

import com.unir.apigateway.dto.MessageDTO;
import com.unir.apigateway.service.MessageService;
import com.unir.common.dto.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class MessageController {
    
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/threads/{threadId}/messages")
    public ResponseEntity<Page<MessageDTO>> listMessages(
            @PathVariable UUID threadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<MessageDTO> messages = messageService.listMessages(threadId, pageRequest);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        MessageDTO message = messageService.sendMessage(request);
        return ResponseEntity.ok(message);
    }
}

