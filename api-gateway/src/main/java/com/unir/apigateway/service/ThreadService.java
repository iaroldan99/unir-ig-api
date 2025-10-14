package com.unir.apigateway.service;

import com.unir.apigateway.dto.ThreadDTO;
import com.unir.apigateway.repository.ThreadRepository;
import com.unir.common.entity.Thread;
import com.unir.common.model.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ThreadService {
    
    private final ThreadRepository threadRepository;

    public ThreadService(ThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    public Page<ThreadDTO> listThreads(Channel channel, UUID accountId, String query, Pageable pageable) {
        Page<Thread> threads;
        
        if (query != null && !query.isBlank()) {
            threads = threadRepository.searchThreads(query, pageable);
        } else if (channel != null && accountId != null) {
            threads = threadRepository.findByChannelAndAccountIdOrderByLastMessageAtDesc(
                    channel, accountId, pageable);
        } else if (accountId != null) {
            threads = threadRepository.findByAccountIdOrderByLastMessageAtDesc(accountId, pageable);
        } else if (channel != null) {
            threads = threadRepository.findByChannelOrderByLastMessageAtDesc(channel, pageable);
        } else {
            threads = threadRepository.findAllByOrderByLastMessageAtDesc(pageable);
        }
        
        return threads.map(ThreadDTO::fromEntity);
    }

    public Optional<ThreadDTO> getThreadById(UUID id) {
        return threadRepository.findById(id)
                .map(ThreadDTO::fromEntity);
    }
}

