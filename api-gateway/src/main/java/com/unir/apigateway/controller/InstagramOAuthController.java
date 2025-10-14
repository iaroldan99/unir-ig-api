package com.unir.apigateway.controller;

import com.unir.apigateway.dto.InstagramConnectionDTO;
import com.unir.apigateway.dto.OAuthCallbackResponse;
import com.unir.apigateway.service.InstagramOAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/instagram")
public class InstagramOAuthController {
    
    private static final Logger log = LoggerFactory.getLogger(InstagramOAuthController.class);
    
    private final InstagramOAuthService instagramOAuthService;

    public InstagramOAuthController(InstagramOAuthService instagramOAuthService) {
        this.instagramOAuthService = instagramOAuthService;
    }

    /**
     * Inicia el flujo OAuth para conectar Instagram
     * 
     * Frontend llama a este endpoint y luego redirige al usuario a la URL retornada
     * 
     * GET /auth/instagram/connect?userId=<uuid>
     */
    @GetMapping("/connect")
    public RedirectView connectInstagram(@RequestParam UUID userId) {
        log.info("Iniciando flujo OAuth de Instagram para user: {}", userId);
        
        String authUrl = instagramOAuthService.getAuthorizationUrl(userId);
        
        // Redirigir al usuario a Instagram para autorizar
        return new RedirectView(authUrl);
    }

    /**
     * Callback de Instagram después de que el usuario autoriza
     * 
     * Instagram redirige aquí: /auth/instagram/callback?code=ABC123&state=user-uuid
     * 
     * Este endpoint procesa el código, obtiene el token y guarda la cuenta
     */
    @GetMapping("/callback")
    public RedirectView handleCallback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_reason,
            @RequestParam(required = false) String error_description) {
        
        // Si el usuario rechazó la autorización
        if (error != null) {
            log.warn("Usuario rechazó autorización de Instagram: {} - {}", error, error_description);
            return new RedirectView("http://localhost:3000/settings/accounts?error=instagram_denied");
        }
        
        try {
            log.info("Procesando callback de Instagram con code: {}...", code.substring(0, 10));
            
            UUID accountId = instagramOAuthService.handleCallback(code, state);
            
            log.info("Instagram conectado exitosamente. Account ID: {}", accountId);
            
            // Redirigir al frontend con éxito
            return new RedirectView(String.format(
                    "http://localhost:3000/settings/accounts?connected=instagram&accountId=%s", 
                    accountId));
            
        } catch (Exception e) {
            log.error("Error procesando callback de Instagram", e);
            return new RedirectView("http://localhost:3000/settings/accounts?error=instagram_failed");
        }
    }

    /**
     * Obtiene el estado de la conexión de Instagram del usuario
     * 
     * GET /auth/instagram/status?userId=<uuid>
     * 
     * Response:
     * {
     *   "connected": true,
     *   "accountId": "uuid",
     *   "displayName": "Mi Tienda",
     *   "username": "mitienda_oficial",
     *   "instagramUserId": "17841...",
     *   "connectedAt": "2025-01-14T10:30:00Z"
     * }
     */
    @GetMapping("/status")
    public ResponseEntity<InstagramConnectionDTO> getConnectionStatus(@RequestParam UUID userId) {
        log.debug("Verificando estado de conexión Instagram para user: {}", userId);
        
        InstagramConnectionDTO status = instagramOAuthService.getConnectionStatus(userId);
        return ResponseEntity.ok(status);
    }

    /**
     * Desconecta la cuenta de Instagram del usuario
     * 
     * POST /auth/instagram/disconnect?userId=<uuid>
     */
    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnectInstagram(@RequestParam UUID userId) {
        log.info("Desconectando Instagram para user: {}", userId);
        
        instagramOAuthService.disconnect(userId);
        
        Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Instagram desconectado exitosamente");
        response.put("status", "disconnected");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint helper para obtener la authorization URL sin redireccionar
     * Útil si el frontend quiere manejar la redirección manualmente
     * 
     * GET /auth/instagram/authorization-url?userId=<uuid>
     */
    @GetMapping("/authorization-url")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl(@RequestParam UUID userId) {
        String authUrl = instagramOAuthService.getAuthorizationUrl(userId);
        
        Map<String, String> response = new java.util.HashMap<>();
        response.put("authorizationUrl", authUrl);
        response.put("userId", userId.toString());
        
        return ResponseEntity.ok(response);
    }
}

