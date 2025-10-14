package com.unir.common.entity;

import com.unir.common.model.Channel;
import com.unir.common.model.Direction;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "messages", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"thread_id", "external_message_id"})
})
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "thread_id", nullable = false)
    private UUID threadId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Direction direction;
    
    @Column(name = "external_message_id")
    private String externalMessageId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> sender;
    
    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;
    
    @Column(length = 50)
    private String status;
    
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Message() {
        this.createdAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getThreadId() {
        return threadId;
    }

    public void setThreadId(UUID threadId) {
        this.threadId = threadId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getExternalMessageId() {
        return externalMessageId;
    }

    public void setExternalMessageId(String externalMessageId) {
        this.externalMessageId = externalMessageId;
    }

    public Map<String, String> getSender() {
        return sender;
    }

    public void setSender(Map<String, String> sender) {
        this.sender = sender;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
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

