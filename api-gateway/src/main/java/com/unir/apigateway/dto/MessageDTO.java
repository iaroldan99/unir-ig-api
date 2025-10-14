package com.unir.apigateway.dto;

import com.unir.common.entity.Message;
import com.unir.common.model.Channel;
import com.unir.common.model.Direction;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public class MessageDTO {
    
    private UUID id;
    private UUID threadId;
    private Channel channel;
    private Direction direction;
    private String externalMessageId;
    private Map<String, String> sender;
    private String bodyText;
    private String status;
    private OffsetDateTime createdAt;

    public MessageDTO() {
    }

    public static MessageDTO fromEntity(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setThreadId(message.getThreadId());
        dto.setChannel(message.getChannel());
        dto.setDirection(message.getDirection());
        dto.setExternalMessageId(message.getExternalMessageId());
        dto.setSender(message.getSender());
        dto.setBodyText(message.getBodyText());
        dto.setStatus(message.getStatus());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
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

