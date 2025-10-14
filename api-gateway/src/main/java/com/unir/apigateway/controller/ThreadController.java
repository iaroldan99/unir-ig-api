package com.unir.apigateway.controller;

import com.unir.apigateway.dto.ThreadDTO;
import com.unir.apigateway.service.ThreadService;
import com.unir.common.model.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/threads")
public class ThreadController {
    
    private final ThreadService threadService;

    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    @GetMapping
    public ResponseEntity<Page<ThreadDTO>> listThreads(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Channel channelEnum = channel != null ? Channel.valueOf(channel.toUpperCase()) : null;
        
        Page<ThreadDTO> threads = threadService.listThreads(channelEnum, accountId, q, pageRequest);
        return ResponseEntity.ok(threads);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThreadDTO> getThread(@PathVariable UUID id) {
        return threadService.getThreadById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

