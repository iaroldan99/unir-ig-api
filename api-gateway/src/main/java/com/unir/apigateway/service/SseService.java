package com.unir.apigateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {
    
    private static final Logger log = LoggerFactory.getLogger(SseService.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutos
    
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        emitter.onCompletion(() -> {
            log.debug("SSE completado, removiendo emitter");
            emitters.remove(emitter);
        });
        
        emitter.onTimeout(() -> {
            log.debug("SSE timeout, removiendo emitter");
            emitters.remove(emitter);
        });
        
        emitter.onError((e) -> {
            log.error("Error en SSE, removiendo emitter", e);
            emitters.remove(emitter);
        });
        
        emitters.add(emitter);
        log.info("Nuevo cliente SSE conectado. Total: {}", emitters.size());
        
        return emitter;
    }

    public void emitEvent(String eventType, Object data) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
                log.debug("Evento {} enviado a cliente SSE", eventType);
            } catch (IOException e) {
                log.error("Error enviando evento SSE", e);
                deadEmitters.add(emitter);
            }
        }
        
        emitters.removeAll(deadEmitters);
    }
}

