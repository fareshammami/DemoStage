package com.example.demo.services;

import com.example.demo.aggregate.PrestationAggregate;
import com.example.demo.command.PrestationCreateCommand;
import com.example.demo.configs.EventStoreRepository;
import com.example.demo.event.PrestationCreated;
import com.example.demo.entities.Prestation;
import com.example.demo.event.PrestationEventHandlers;
import com.example.demo.repository.PrestationRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PrestationService {

    @Autowired
    private EventStoreRepository eventStoreRepository;

    @Autowired
    private PrestationRepo prestationRepo;
    @Autowired
    private PrestationEventHandlers prestationEventHandler;
    @Autowired
    private ObjectMapper objectMapper;

    public CompletableFuture<Void> createPrestation(PrestationCreateCommand command) throws Exception {
        PrestationAggregate aggregate = new PrestationAggregate();
        List<Object> events = aggregate.handle(command);

        // Save events to EventStore
        return eventStoreRepository.save(command.getId(), events)
                .thenAccept(v -> {
                    // Apply events to the aggregate
                    events.forEach(event -> {
                        if (event instanceof PrestationCreated) {
                            aggregate.apply((PrestationCreated) event);
                            prestationEventHandler.handle((PrestationCreated) event);
                        }
                        // Handle other events if needed
                    });
                });
    }

    public List<Prestation> getAllPrestations() {
        return prestationRepo.findAll();
    }

    public Prestation getPrestationById(String id) {
        return prestationRepo.findById(id).orElse(null);
    }
}
