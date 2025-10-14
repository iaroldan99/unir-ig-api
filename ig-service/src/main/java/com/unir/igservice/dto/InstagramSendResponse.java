package com.unir.igservice.dto;

public class InstagramSendResponse {
    
    private String messageId;
    private String status;

    public InstagramSendResponse() {
    }

    public InstagramSendResponse(String messageId, String status) {
        this.messageId = messageId;
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

