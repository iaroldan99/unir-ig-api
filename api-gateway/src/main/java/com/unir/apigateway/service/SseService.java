package com.unir.apigateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Service
public class SseService {
    
    private static final Logger log = LoggerFactory.getLogger(SseService.class);

    private final Sinks.Many<ServerSentEvent<String>> sink;

    public SseService() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    public Flux<ServerSentEvent<String>> getEventStream() {
        log.info("Cliente conectado al stream SSE");
        
        return sink.asFlux()
                .mergeWith(Flux.interval(Duration.ofSeconds(30)) // Keep-alive cada 30s
                        .map(sequence -> ServerSentEvent.<String>builder()
                                .event("heartbeat")
                                .data(String.valueOf(sequence))
                                .build()));
    }

    public void emitEvent(String eventType, Object data) {
        String dataStr = data instanceof String ? (String) data : data.toString();
        
        log.debug("Emitiendo evento SSE: type={}, data={}", eventType, dataStr);
        
        sink.tryEmitNext(ServerSentEvent.<String>builder()
                .event(eventType)
                .data(dataStr)
                .build());
    }
}
