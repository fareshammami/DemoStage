package com.example.demo.controllers;

import com.eventstore.dbclient.ConnectionShutdownException;
import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.WriteResult;
import com.example.demo.entities.Prestation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
public class PrestationControlleur {

    private static final int MAX_RETRIES = 3;

    private EventStoreDBClient eventStoreDBClient;
    private final ObjectMapper objectMapper;

    public PrestationControlleur(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        initializeEventStoreClient();  // Initialize the client in the constructor
    }

    @PostMapping("/createPrestation")
    public ResponseEntity<String> createPrestation(@RequestBody Prestation prestation) {
        EventData eventData = createEventData(prestation);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (eventStoreDBClient == null || eventStoreDBClient.isShutdown()) {
                    initializeEventStoreClient();  // Re-initialize the client if it's closed
                }

                WriteResult result = eventStoreDBClient.appendToStream("prestation-stream", eventData).get();
                return ResponseEntity.ok("Prestation created successfully. Position: " + result.getLogPosition());
            } catch (ConnectionShutdownException | InterruptedException | ExecutionException e) {
                if (attempt == MAX_RETRIES) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to create prestation after " + MAX_RETRIES + " attempts. Error: " + e.getMessage());
                } else {
                    System.out.println("Retrying to create prestation... Attempt " + attempt);
                }
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create prestation.");
    }

    private void initializeEventStoreClient() {
        try {
            EventStoreDBClientSettings settings = EventStoreDBClientSettings
                    .builder()
                    .addHost(new InetSocketAddress("localhost", 2113))  // Replace with your host and port
                    .tls(false)  // Assuming no TLS for local dev, adjust as needed
                    .defaultCredentials("admin", "changeit")  // Replace with your credentials
                    .buildConnectionSettings();  // Correct method to finalize settings

            eventStoreDBClient = EventStoreDBClient.create(settings);
        } catch (Exception e) {
            System.err.println("Failed to initialize EventStoreDB client: " + e.getMessage());
        }
    }

    private EventData createEventData(Prestation prestation) {
        try {
            // Convert the Prestation object to JSON
            String jsonData = objectMapper.writeValueAsString(prestation);

            // Create EventData with the JSON data
            return EventData.builderAsBinary("prestation-event", jsonData.getBytes())
                    .eventId(UUID.randomUUID())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Prestation to JSON", e);
        }
    }
}
