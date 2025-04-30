package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.Exchange;
import edu.famu.thebookexchange.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/Exchanges")
public class ExchangeController {

    private final ExchangeService exchangeService;

    @Autowired
    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> requestBookExchange(@RequestBody Map<String, String> payload) {
        String offeredBookId = payload.get("offeredBookId");
        String requestedBookId = payload.get("requestedBookId");
        String requesterId = payload.get("requesterId");
        String ownerId = payload.get("ownerId");

        if (offeredBookId == null || requestedBookId == null || requesterId == null || ownerId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing required fields for exchange request."));
        }

        try {
            String exchangeId = exchangeService.requestExchange(offeredBookId, requestedBookId, requesterId, ownerId);
            return ResponseEntity.ok(Map.of("success", true, "exchangeId", exchangeId, "message", "Exchange requested successfully."));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to request exchange: " + e.getMessage()));
        }
    }

    @PostMapping("/{exchangeId}/respond")
    public ResponseEntity<Map<String, Object>> respondToBookExchange(
            @PathVariable String exchangeId,
            @RequestBody Map<String, String> payload) {
        String action = payload.get("action");
        String responderId = payload.get("responderId");

        if (action == null || responderId == null || (!action.equals("accepted") && !action.equals("rejected"))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid action or missing responder ID. Action must be 'accepted' or 'rejected'."));
        }

        try {
            boolean success = exchangeService.respondToExchange(exchangeId, action, responderId);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Exchange " + action + "."));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to respond to exchange or invalid request."));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to respond to exchange: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Exchange>> getExchangesForUser(@PathVariable String userId) {
        try {
            List<Exchange> exchanges = exchangeService.getExchangesForUser(userId);
            return ResponseEntity.ok(exchanges);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.internalServerError().body(List.of()); // Or a more informative error response
        }
    }
}