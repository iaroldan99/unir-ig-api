package com.unir.igservice.service;

import com.unir.common.entity.Message;
import com.unir.common.entity.Thread;
import com.unir.common.model.Channel;
import com.unir.common.model.Direction;
import com.unir.igservice.dto.InstagramWebhookPayload;
import com.unir.igservice.repository.MessageRepository;
import com.unir.igservice.repository.ThreadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class InstagramWebhookService {
    
    private static final Logger log = LoggerFactory.getLogger(InstagramWebhookService.class);
    
    private final ThreadRepository threadRepository;
    private final MessageRepository messageRepository;

    public InstagramWebhookService(ThreadRepository threadRepository, MessageRepository messageRepository) {
        this.threadRepository = threadRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public void processWebhook(InstagramWebhookPayload payload) {
        if (payload.getEntry() == null) {
            return;
        }

        for (InstagramWebhookPayload.Entry entry : payload.getEntry()) {
            if (entry.getMessaging() == null) {
                continue;
            }

            for (InstagramWebhookPayload.MessagingEvent event : entry.getMessaging()) {
                processMessagingEvent(event);
            }
        }
    }

    private void processMessagingEvent(InstagramWebhookPayload.MessagingEvent event) {
        if (event.getMessage() == null) {
            return;
        }

        String senderId = event.getSender().getId();
        String recipientId = event.getRecipient().getId();
        String externalThreadId = senderId; // Usar sender como thread ID
        
        // Por simplicidad, usamos un accountId fijo. En producción buscarías por recipientId
        UUID accountId = UUID.randomUUID(); // Aquí deberías buscar la cuenta por recipientId
        
        // Buscar o crear thread
        Thread thread = threadRepository.findByAccountIdAndExternalThreadId(accountId, externalThreadId)
                .orElseGet(() -> {
                    Thread newThread = new Thread();
                    newThread.setAccountId(accountId);
                    newThread.setChannel(Channel.INSTAGRAM);
                    newThread.setExternalThreadId(externalThreadId);
                    
                    Thread.Participant participant = new Thread.Participant(senderId, "Instagram User");
                    newThread.setParticipants(List.of(participant));
                    newThread.setLastMessageAt(OffsetDateTime.now());
                    
                    return threadRepository.save(newThread);
                });

        // Verificar si el mensaje ya existe (idempotencia)
        String externalMessageId = event.getMessage().getMid();
        Optional<Message> existing = messageRepository.findByThreadIdAndExternalMessageId(
                thread.getId(), externalMessageId);
        
        if (existing.isPresent()) {
            log.debug("Mensaje {} ya existe, saltando", externalMessageId);
            return;
        }

        // Crear mensaje
        Message message = new Message();
        message.setThreadId(thread.getId());
        message.setChannel(Channel.INSTAGRAM);
        message.setDirection(Direction.INBOUND);
        message.setExternalMessageId(externalMessageId);
        
        Map<String, String> sender = new HashMap<>();
        sender.put("id", senderId);
        sender.put("name", "Instagram User");
        message.setSender(sender);
        
        message.setBodyText(event.getMessage().getText());
        message.setStatus("received");
        
        if (event.getTimestamp() != null) {
            OffsetDateTime createdAt = OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(event.getTimestamp()), ZoneOffset.UTC);
            message.setCreatedAt(createdAt);
        }

        messageRepository.save(message);
        
        // Actualizar thread
        thread.setLastMessageAt(message.getCreatedAt());
        threadRepository.save(thread);
        
        log.info("Mensaje procesado: {} en thread {}", message.getId(), thread.getId());
    }
}

