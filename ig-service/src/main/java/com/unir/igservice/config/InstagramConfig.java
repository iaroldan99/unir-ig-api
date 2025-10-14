package com.unir.igservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "instagram")
public class InstagramConfig {
    
    private String verifyToken;
    private String graphVersion;
    private String pageAccessToken;
    private String userId;
    private String graphApiBaseUrl;

    public String getVerifyToken() {
        return verifyToken;
    }

    public void setVerifyToken(String verifyToken) {
        this.verifyToken = verifyToken;
    }

    public String getGraphVersion() {
        return graphVersion;
    }

    public void setGraphVersion(String graphVersion) {
        this.graphVersion = graphVersion;
    }

    public String getPageAccessToken() {
        return pageAccessToken;
    }

    public void setPageAccessToken(String pageAccessToken) {
        this.pageAccessToken = pageAccessToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGraphApiBaseUrl() {
        return graphApiBaseUrl;
    }

    public void setGraphApiBaseUrl(String graphApiBaseUrl) {
        this.graphApiBaseUrl = graphApiBaseUrl;
    }
}

