package com.unir.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unir.apigateway.config.InstagramOAuthConfig;
import com.unir.apigateway.dto.InstagramConnectionDTO;
import com.unir.apigateway.repository.AccountRepository;
import com.unir.common.entity.Account;
import com.unir.common.model.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class InstagramOAuthService {
    
    private static final Logger log = LoggerFactory.getLogger(InstagramOAuthService.class);
    
    private final InstagramOAuthConfig config;
    private final AccountRepository accountRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public InstagramOAuthService(InstagramOAuthConfig config,
                                 AccountRepository accountRepository,
                                 WebClient webClient,
                                 ObjectMapper objectMapper) {
        this.config = config;
        this.accountRepository = accountRepository;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Genera la URL de autorización para que el usuario inicie el flujo OAuth
     */
    public String getAuthorizationUrl(UUID userId) {
        return UriComponentsBuilder
                .fromHttpUrl(config.getAuthorizationUrl())
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("scope", config.getScopes())
                .queryParam("response_type", "code")
                .queryParam("state", userId.toString()) // Para identificar al usuario después del callback
                .build()
                .toUriString();
    }

    /**
     * Intercambia el código de autorización por un access token
     * y guarda la cuenta del usuario
     */
    @Transactional
    public UUID handleCallback(String code, String state) throws Exception {
        log.info("Procesando callback de Instagram OAuth para user_id: {}", state);
        
        UUID userId = UUID.fromString(state);
        
        // 1. Intercambiar code por access_token
        Map<String, String> tokenResponse = exchangeCodeForToken(code);
        String accessToken = tokenResponse.get("access_token");
        String igUserId = tokenResponse.get("user_id");
        
        log.info("Access token obtenido para IG user_id: {}", igUserId);
        
        // 2. Obtener información del perfil de Instagram
        Map<String, String> profileInfo = getInstagramProfile(accessToken, igUserId);
        
        // 3. Obtener el Instagram Business Account ID (si aplicable)
        String businessAccountId = getBusinessAccountId(accessToken);
        
        // 4. Buscar o crear account
        Account account = accountRepository.findByUserIdAndChannelAndStatus(userId, Channel.INSTAGRAM, "active")
                .orElse(new Account());
        
        account.setUserId(userId);
        account.setChannel(Channel.INSTAGRAM);
        account.setDisplayName(profileInfo.get("name"));
        
        // External IDs
        Map<String, String> externalIds = new HashMap<>();
        externalIds.put("ig_user_id", businessAccountId != null ? businessAccountId : igUserId);
        externalIds.put("user_id", igUserId);
        externalIds.put("username", profileInfo.get("username"));
        account.setExternalIds(externalIds);
        
        // TODO: Cifrar con AES-256-GCM usando CryptoUtil
        // Por ahora guardamos sin cifrar (SOLO PARA DESARROLLO)
        Map<String, String> credentials = new HashMap<>();
        credentials.put("access_token", accessToken);
        credentials.put("token_type", "bearer");
        account.setCredentialsEncrypted(objectMapper.writeValueAsString(credentials));
        
        account.setStatus("active");
        account.setCreatedAt(OffsetDateTime.now());
        
        Account saved = accountRepository.save(account);
        log.info("Cuenta de Instagram guardada con ID: {}", saved.getId());
        
        return saved.getId();
    }

    /**
     * Intercambia el código por access token
     */
    private Map<String, String> exchangeCodeForToken(String code) throws Exception {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", config.getClientId());
        formData.add("client_secret", config.getClientSecret());
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", config.getRedirectUri());
        formData.add("code", code);

        String response = webClient.post()
                .uri(config.getTokenUrl())
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode jsonNode = objectMapper.readTree(response);
        
        Map<String, String> result = new HashMap<>();
        result.put("access_token", jsonNode.get("access_token").asText());
        result.put("user_id", jsonNode.get("user_id").asText());
        
        return result;
    }

    /**
     * Obtiene información básica del perfil
     */
    private Map<String, String> getInstagramProfile(String accessToken, String userId) throws Exception {
        String url = String.format("https://graph.instagram.com/%s?fields=id,username&access_token=%s", 
                                    userId, accessToken);

        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode jsonNode = objectMapper.readTree(response);
        
        Map<String, String> result = new HashMap<>();
        result.put("username", jsonNode.get("username").asText());
        result.put("name", jsonNode.has("name") ? jsonNode.get("name").asText() : jsonNode.get("username").asText());
        
        return result;
    }

    /**
     * Intenta obtener el Instagram Business Account ID
     * (Para cuentas business conectadas a páginas de Facebook)
     */
    private String getBusinessAccountId(String accessToken) {
        try {
            // Obtener páginas del usuario
            String url = String.format("https://graph.facebook.com/v24.0/me/accounts?fields=instagram_business_account&access_token=%s", 
                                        accessToken);

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("data") && jsonNode.get("data").size() > 0) {
                JsonNode firstPage = jsonNode.get("data").get(0);
                if (firstPage.has("instagram_business_account")) {
                    return firstPage.get("instagram_business_account").get("id").asText();
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener Business Account ID: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * Verifica si el usuario tiene Instagram conectado
     */
    public InstagramConnectionDTO getConnectionStatus(UUID userId) {
        Optional<Account> accountOpt = accountRepository.findByUserIdAndChannelAndStatus(
                userId, Channel.INSTAGRAM, "active");
        
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            String username = account.getExternalIds().get("username");
            String igUserId = account.getExternalIds().get("ig_user_id");
            
            return InstagramConnectionDTO.connected(
                    account.getId(),
                    account.getDisplayName(),
                    username,
                    igUserId,
                    account.getCreatedAt()
            );
        }
        
        return InstagramConnectionDTO.notConnected();
    }

    /**
     * Desconecta la cuenta de Instagram
     */
    @Transactional
    public void disconnect(UUID userId) {
        accountRepository.findByUserIdAndChannelAndStatus(userId, Channel.INSTAGRAM, "active")
                .ifPresent(account -> {
                    account.setStatus("inactive");
                    accountRepository.save(account);
                    log.info("Cuenta de Instagram desconectada para user: {}", userId);
                });
    }
}

