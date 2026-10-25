package com.infeara.site.api;

import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api")
public class ConsultationController {
    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/consultations")
    public ResponseEntity<Map<String, String>> submit(@Valid @RequestBody ConsultationRequest request) {
        // Quietly accept a filled honeypot so automated form bots don't learn the trap.
        if (request.website() != null && !request.website().isBlank()) {
            return ResponseEntity.accepted().body(Map.of("message", "Thank you. Your request has been received."));
        }
        try {
            consultationService.save(request);
            return ResponseEntity.accepted().body(Map.of("message", "Thank you. Your request has been received."));
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The enquiry service is temporarily unavailable.");
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The enquiry could not be delivered. Please try again shortly.");
        }
    }
}
