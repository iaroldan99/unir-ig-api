package com.unir.igservice.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unir.igservice.config.InstagramConfig;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstagramConfig instagramConfig;

    @Test
    void verifyWebhook_WithCorrectToken_ReturnsChallenge() throws Exception {
        // Arrange
        String verifyToken = "demo_token";
        String challenge = "test_challenge_123";
        when(instagramConfig.getVerifyToken()).thenReturn(verifyToken);

        // Act & Assert
        mockMvc.perform(get("/webhooks/instagram")
                        .param("hub.mode", "subscribe")
                        .param("hub.challenge", challenge)
                        .param("hub.verify_token", verifyToken))
                .andExpect(status().isOk())
                .andExpect(content().string(challenge));
    }

    @Test
    void verifyWebhook_WithIncorrectToken_ReturnsForbidden() throws Exception {
        // Arrange
        when(instagramConfig.getVerifyToken()).thenReturn("correct_token");

        // Act & Assert
        mockMvc.perform(get("/webhooks/instagram")
                        .param("hub.mode", "subscribe")
                        .param("hub.challenge", "test_challenge")
                        .param("hub.verify_token", "wrong_token"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Forbidden"));
    }

    @Test
    void verifyWebhook_WithInvalidMode_ReturnsForbidden() throws Exception {
        // Arrange
        when(instagramConfig.getVerifyToken()).thenReturn("demo_token");

        // Act & Assert
        mockMvc.perform(get("/webhooks/instagram")
                        .param("hub.mode", "invalid_mode")
                        .param("hub.challenge", "test_challenge")
                        .param("hub.verify_token", "demo_token"))
                .andExpect(status().isForbidden());
    }
}

