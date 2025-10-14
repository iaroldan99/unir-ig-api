package com.unir.common.entity;

import com.unir.common.model.Channel;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;
    
    @Column(name = "display_name")
    private String displayName;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_ids", columnDefinition = "jsonb")
    private Map<String, String> externalIds;
    
    @Column(name = "credentials_encrypted", columnDefinition = "TEXT")
    private String credentialsEncrypted;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Account() {
        this.createdAt = OffsetDateTime.now();
        this.status = "active";
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, String> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(Map<String, String> externalIds) {
        this.externalIds = externalIds;
    }

    public String getCredentialsEncrypted() {
        return credentialsEncrypted;
    }

    public void setCredentialsEncrypted(String credentialsEncrypted) {
        this.credentialsEncrypted = credentialsEncrypted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

