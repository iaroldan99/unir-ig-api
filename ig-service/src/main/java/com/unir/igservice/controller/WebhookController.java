package com.unir.igservice.controller;

import com.unir.igservice.config.InstagramConfig;
import com.unir.igservice.dto.InstagramWebhookPayload;
import com.unir.igservice.service.InstagramWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/instagram")
public class WebhookController {
    
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    
    private final InstagramConfig config;
    private final InstagramWebhookService webhookService;

    public WebhookController(InstagramConfig config, InstagramWebhookService webhookService) {
        this.config = config;
        this.webhookService = webhookService;
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
        log.info("Recibido webhook de Instagram: {}", payload);
        
        try {
            webhookService.processWebhook(payload);
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception e) {
            log.error("Error procesando webhook", e);
            return ResponseEntity.status(500).body("ERROR");
        }
    }
}

