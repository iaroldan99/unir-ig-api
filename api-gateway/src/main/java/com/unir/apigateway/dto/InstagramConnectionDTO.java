package com.unir.apigateway.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class InstagramConnectionDTO {
    
    private UUID accountId;
    private String displayName;
    private String username;
    private String instagramUserId;
    private boolean connected;
    private OffsetDateTime connectedAt;

    public InstagramConnectionDTO() {
    }

    public static InstagramConnectionDTO connected(UUID accountId, String displayName, String username, String instagramUserId, OffsetDateTime connectedAt) {
        InstagramConnectionDTO dto = new InstagramConnectionDTO();
        dto.setAccountId(accountId);
        dto.setDisplayName(displayName);
        dto.setUsername(username);
        dto.setInstagramUserId(instagramUserId);
        dto.setConnected(true);
        dto.setConnectedAt(connectedAt);
        return dto;
    }

    public static InstagramConnectionDTO notConnected() {
        InstagramConnectionDTO dto = new InstagramConnectionDTO();
        dto.setConnected(false);
        return dto;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getInstagramUserId() {
        return instagramUserId;
    }

    public void setInstagramUserId(String instagramUserId) {
        this.instagramUserId = instagramUserId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public OffsetDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(OffsetDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }
}

