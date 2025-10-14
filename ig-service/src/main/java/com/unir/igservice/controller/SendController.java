package com.unir.igservice.controller;

import com.unir.igservice.dto.InstagramSendRequest;
import com.unir.igservice.dto.InstagramSendResponse;
import com.unir.igservice.service.InstagramSendService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/ig")
public class SendController {
    
    private static final Logger log = LoggerFactory.getLogger(SendController.class);
    
    private final InstagramSendService sendService;

    public SendController(InstagramSendService sendService) {
        this.sendService = sendService;
    }

    @PostMapping("/send")
    public ResponseEntity<InstagramSendResponse> sendMessage(@Valid @RequestBody InstagramSendRequest request) {
        log.info("Enviando mensaje de Instagram a {}", request.getToId());
        
        try {
            InstagramSendResponse response = sendService.sendMessage(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error enviando mensaje", e);
            return ResponseEntity.status(500).body(new InstagramSendResponse(null, "error"));
        }
    }
}

