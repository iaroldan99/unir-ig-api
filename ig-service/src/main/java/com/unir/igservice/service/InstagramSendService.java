package com.unir.igservice.service;

import com.unir.igservice.config.InstagramConfig;
import com.unir.igservice.dto.InstagramSendRequest;
import com.unir.igservice.dto.InstagramSendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class InstagramSendService {
    
    private static final Logger log = LoggerFactory.getLogger(InstagramSendService.class);
    
    private final WebClient webClient;
    private final InstagramConfig config;

    public InstagramSendService(WebClient webClient, InstagramConfig config) {
        this.webClient = webClient;
        this.config = config;
    }

    public InstagramSendResponse sendMessage(InstagramSendRequest request) {
        String url = String.format("%s/%s/%s/messages",
                config.getGraphApiBaseUrl(),
                config.getGraphVersion(),
                config.getUserId());

        Map<String, Object> body = new HashMap<>();
        body.put("recipient", Map.of("id", request.getToId()));
        body.put("message", Map.of("text", request.getText()));

        try {
            Map<String, Object> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("access_token", config.getPageAccessToken())
                            .build())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .block();

            String messageId = response != null ? (String) response.get("message_id") : null;
            log.info("Mensaje enviado exitosamente: {}", messageId);
            
            return new InstagramSendResponse(messageId, "sent");
        } catch (Exception e) {
            log.error("Error al enviar mensaje a Instagram", e);
            throw new RuntimeException("Failed to send message", e);
        }
    }
}

