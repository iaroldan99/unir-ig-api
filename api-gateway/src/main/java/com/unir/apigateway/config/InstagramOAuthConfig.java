package com.unir.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "instagram.oauth")
public class InstagramOAuthConfig {
    
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    // Para Instagram Graph API usamos Facebook Login (no Instagram Basic Display)
    private String authorizationUrl = "https://www.facebook.com/v24.0/dialog/oauth";
    private String tokenUrl = "https://graph.facebook.com/v24.0/oauth/access_token";
    
    // Para Instagram Business necesitamos estos scopes de Facebook + Instagram
    private String scopes = "pages_show_list,pages_read_engagement,pages_manage_metadata,instagram_basic,instagram_manage_messages,business_management";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }
}

