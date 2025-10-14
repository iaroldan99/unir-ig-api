package com.unir.common.entity;

import com.unir.common.model.Channel;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "threads", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "external_thread_id"})
})
public class Thread {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;
    
    @Column(name = "external_thread_id", nullable = false)
    private String externalThreadId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Participant> participants;
    
    @Column(name = "last_message_at")
    private OffsetDateTime lastMessageAt;

    public Thread() {
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

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public OffsetDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(OffsetDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public static class Participant {
        private String id;
        private String name;

        public Participant() {
        }

        public Participant(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

