package com.unir.apigateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Value("${services.ig-service.url:http://localhost:8081}")
    private String igServiceUrl;
    
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }
    
    @Bean
    @Qualifier("igServiceWebClient")
    public WebClient igServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(igServiceUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }
}

