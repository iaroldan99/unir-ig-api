package com.unir.common.dto;

import com.unir.common.model.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class SendMessageRequest {
    
    @NotNull
    private Channel channel;
    
    @NotNull
    private UUID accountId;
    
    private UUID threadId;
    
    @NotNull
    private List<Recipient> to;
    
    private String text;
    
    private String html;
    
    private List<Attachment> attachments;

    public SendMessageRequest() {
    }

    // Getters and Setters
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getThreadId() {
        return threadId;
    }

    public void setThreadId(UUID threadId) {
        this.threadId = threadId;
    }

    public List<Recipient> getTo() {
        return to;
    }

    public void setTo(List<Recipient> to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public static class Recipient {
        @NotBlank
        private String id;

        public Recipient() {
        }

        public Recipient(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class Attachment {
        private String url;
        private String type;

        public Attachment() {
        }

        public Attachment(String url, String type) {
            this.url = url;
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}

