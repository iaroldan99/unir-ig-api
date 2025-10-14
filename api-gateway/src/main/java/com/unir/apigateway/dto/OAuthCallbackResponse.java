package com.unir.apigateway.dto;

import java.util.UUID;

public class OAuthCallbackResponse {
    
    private boolean success;
    private String message;
    private UUID accountId;
    private String redirectUrl; // Para redirigir al frontend después del callback

    public OAuthCallbackResponse() {
    }

    public static OAuthCallbackResponse success(UUID accountId, String message) {
        OAuthCallbackResponse response = new OAuthCallbackResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setAccountId(accountId);
        response.setRedirectUrl("http://localhost:3000/settings/accounts?connected=instagram");
        return response;
    }

    public static OAuthCallbackResponse error(String message) {
        OAuthCallbackResponse response = new OAuthCallbackResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setRedirectUrl("http://localhost:3000/settings/accounts?error=instagram");
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}

