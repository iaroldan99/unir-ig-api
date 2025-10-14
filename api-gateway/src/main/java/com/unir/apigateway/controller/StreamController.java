package com.unir.apigateway.controller;

import com.unir.apigateway.service.SseService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1")
public class StreamController {
    
    private final SseService sseService;

    public StreamController(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * Stream de eventos en tiempo real usando Server-Sent Events (SSE)
     * GET /v1/stream
     * 
     * El cliente se conecta y recibe:
     * - event: "message.sent" cuando se envía un mensaje
     * - event: "message.received" cuando llega un mensaje nuevo
     * - event: "thread.updated" cuando se actualiza un thread
     * - event: "heartbeat" cada 30s para mantener la conexión viva
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream() {
        return sseService.getEventStream();
    }
}
