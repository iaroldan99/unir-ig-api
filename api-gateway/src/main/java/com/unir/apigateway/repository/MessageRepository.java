package com.unir.apigateway.repository;

import com.unir.common.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    
    Page<Message> findByThreadIdOrderByCreatedAtDesc(UUID threadId, Pageable pageable);
    
    List<Message> findByThreadIdOrderByCreatedAtAsc(UUID threadId);
}
