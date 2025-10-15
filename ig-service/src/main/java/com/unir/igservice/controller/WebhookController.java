package com.unir.igservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unir.igservice.config.InstagramConfig;
import com.unir.igservice.dto.InstagramWebhookPayload;
import com.unir.igservice.service.InstagramWebhookService;

@RestController
@RequestMapping("/webhooks/instagram")
public class WebhookController {
    
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    
    private final InstagramConfig config;
    private final InstagramWebhookService webhookService;
    private final ObjectMapper objectMapper;

    public WebhookController(InstagramConfig config, InstagramWebhookService webhookService, ObjectMapper objectMapper) {
        this.config = config;
        this.webhookService = webhookService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge,
            @RequestParam("hub.verify_token") String verifyToken) {
        
        log.info("Verificando webhook - mode: {}, token: {}", mode, verifyToken);
        
        if ("subscribe".equals(mode) && config.getVerifyToken().equals(verifyToken)) {
            log.info("Webhook verificado correctamente");
            return ResponseEntity.ok(challenge);
        }
        
        log.warn("Verificación fallida - token incorrecto");
        return ResponseEntity.status(403).body("Forbidden");
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody InstagramWebhookPayload payload) {
        try {
            String payloadJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            log.info("📩 Webhook recibido de Instagram:\n{}", payloadJson);
        } catch (Exception e) {
            log.warn("No se pudo serializar el payload: {}", payload);
        }
        
        try {
            webhookService.processWebhook(payload);
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception e) {
            log.error("Error procesando webhook", e);
            return ResponseEntity.status(500).body("ERROR");
        }
    }
}

