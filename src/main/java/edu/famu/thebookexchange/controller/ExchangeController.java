package edu.famu.thebookexchange.controller;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import edu.famu.thebookexchange.model.Rest.Exchange;
import edu.famu.thebookexchange.model.Rest.RestResponse;
import edu.famu.thebookexchange.service.ExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static edu.famu.thebookexchange.service.ExchangeService.EXCHANGES_COLLECTION;
import static edu.famu.thebookexchange.service.ExchangeService.FIRESTORE_TIMEOUT;
import static org.glassfish.hk2.utilities.reflection.Pretty.collection;

@RestController
@RequestMapping("/Exchanges")
public class ExchangeController {

    private final ExchangeService exchangeService;
    private final FirebaseAuth firebaseAuth;
    private static final Logger logger = LoggerFactory.getLogger(ExchangeController.class);
    private final Firestore firestore;

    @Autowired
    public ExchangeController(ExchangeService exchangeService, FirebaseAuth firebaseAuth, Firestore firestore) {
        this.exchangeService = exchangeService;
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    @PostMapping("/request")
    public ResponseEntity<RestResponse> requestBookExchange(@RequestBody Map<String, String> payload) {
        String offeredBookId = payload.get("offeredBookId");
        String requestedBookId = payload.get("requestedBookId");
        String requesterId = payload.get("requesterId");
        String ownerId = payload.get("ownerId");

        logger.info("Received request to create a new exchange. Offered Book ID: {}, Requested Book ID: {}, Requester ID: {}, Owner ID: {}",
                offeredBookId, requestedBookId, requesterId, ownerId);

        if (offeredBookId == null || requestedBookId == null || requesterId == null || ownerId == null) {
            logger.warn("Missing required fields for exchange request.");
            return ResponseEntity.badRequest().body(new RestResponse(false, "Missing required fields for exchange request."));
        }

        try {
            String exchangeId = exchangeService.requestExchange(offeredBookId, requestedBookId, requesterId, ownerId);
            logger.info("Exchange requested successfully. Generated Exchange ID: {}", exchangeId);
            return ResponseEntity.ok(new RestResponse(true, "Exchange requested successfully.", Map.of("exchangeId", exchangeId)));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Failed to request exchange.", e);
            return ResponseEntity.internalServerError().body(new RestResponse(false, "Failed to request exchange: " + e.getMessage()));
        }
    }


    @PostMapping("/{exchangeId}/respond")
    public ResponseEntity<RestResponse> respondToExchange(
            @PathVariable String exchangeId,
            @RequestParam String action,
            @RequestParam String responderId) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            boolean success = exchangeService.respondToExchange(exchangeId, action, responderId);
            if (success) {
                if (action.equalsIgnoreCase("accepted")) {
                    return ResponseEntity.ok(new RestResponse(true, "Exchange " + action + " successfully."));
                } else if (action.equalsIgnoreCase("rejected")) {
                    return ResponseEntity.ok(new RestResponse(true, "Exchange " + action + " successfully."));
                } else {
                    return ResponseEntity.ok(new RestResponse(true, "Exchange response processed."));
                }
            } else {
                DocumentReference exchangeRef = firestore.collection(ExchangeService.EXCHANGES_COLLECTION).document(exchangeId);
                DocumentSnapshot snapshot = exchangeRef.get().get(ExchangeService.FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                if (snapshot.exists() && snapshot.toObject(Exchange.class) != null && !snapshot.toObject(Exchange.class).getStatus().equals("pending")) {
                    return ResponseEntity.badRequest().body(new RestResponse(false, "This exchange has already been responded to."));
                } else {
                    return ResponseEntity.badRequest().body(new RestResponse(false, "Failed to respond to exchange or invalid request."));
                }
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error responding to exchange: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponse(false, "Internal server error."));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RestResponse> getExchangesForUser(@PathVariable String userId) {
        logger.info("Received request to get exchanges for user ID: {}", userId);
        try {
            List<Exchange> exchanges = exchangeService.getExchangesForUser(userId);
            logger.info("Retrieved {} exchanges for user ID: {}", exchanges.size(), userId);
            return ResponseEntity.ok(new RestResponse(true, "Exchanges retrieved successfully.", exchanges));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Failed to retrieve exchanges for user ID: {}", userId, e);
            return ResponseEntity.internalServerError().body(new RestResponse(false, "Failed to retrieve exchanges: " + e.getMessage(), List.of()));
        }
    }

    @PostMapping("/exchanges/direct")
    public ResponseEntity<String> createDirectExchange(
            @RequestParam String offeredBookId,
            @RequestParam String requesterId) {
        try {
            exchangeService.createDirectExchangeRequest(offeredBookId, requesterId);
            return ResponseEntity.ok("Direct exchange request initiated successfully.");
        } catch (RuntimeException e) {
            if (e.getMessage().startsWith("Invalid requester ID")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            } else {
                // Handle other potential RuntimeExceptions (maybe log them)
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initiate direct exchange.");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // Handle other exceptions (maybe log them)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initiate direct exchange due to a server error.");
        }
    }
}
