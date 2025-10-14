package com.unir.igservice.repository;

import com.unir.common.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    Optional<Message> findByThreadIdAndExternalMessageId(UUID threadId, String externalMessageId);
}

