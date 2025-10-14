package com.unir.apigateway.dto;

import com.unir.common.entity.Thread;
import com.unir.common.model.Channel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ThreadDTO {
    
    private UUID id;
    private UUID accountId;
    private Channel channel;
    private String externalThreadId;
    private List<Thread.Participant> participants;
    private OffsetDateTime lastMessageAt;

    public ThreadDTO() {
    }

    public static ThreadDTO fromEntity(Thread thread) {
        ThreadDTO dto = new ThreadDTO();
        dto.setId(thread.getId());
        dto.setAccountId(thread.getAccountId());
        dto.setChannel(thread.getChannel());
        dto.setExternalThreadId(thread.getExternalThreadId());
        dto.setParticipants(thread.getParticipants());
        dto.setLastMessageAt(thread.getLastMessageAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getExternalThreadId() {
        return externalThreadId;
    }

    public void setExternalThreadId(String externalThreadId) {
        this.externalThreadId = externalThreadId;
    }

    public List<Thread.Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Thread.Participant> participants) {
        this.participants = participants;
    }

    public OffsetDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(OffsetDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}

