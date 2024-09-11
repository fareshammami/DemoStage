package com.example.demo.services;

import com.example.demo.aggregate.PaiementAggregate;
import com.example.demo.command.PaiementCreateCommand;
import com.example.demo.configs.EventStoreRepository;
import com.example.demo.entities.StatusEnum;
import com.example.demo.event.IndemnisationCreated;
import com.example.demo.event.PaiementCreated;
import com.example.demo.entities.Paiement;
import com.example.demo.repository.PaiementRepo;
import com.example.demo.event.PaiementEventHandlers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PaiementService {

    @Autowired
    private EventStoreRepository eventStoreRepository;

    @Autowired
    private PaiementRepo paiementRepo;

    @Autowired
    private PaiementEventHandlers paiementEventHandler;

    private final String streamName = "dewdrop.IndemnisationAggregate-f47ac10b-58cc-4372-a567-0e02b6f3d479";

    public CompletableFuture<Void> createPaiement(PaiementCreateCommand command) throws Exception {
        PaiementAggregate aggregate = new PaiementAggregate();
        List<Object> events = aggregate.handle(command);

        // Log the generated events
        events.forEach(event -> System.out.println("Generated event: " + event.getClass().getSimpleName()));

        // Save events to EventStore
        return eventStoreRepository.save(command.getId(), events)
                .thenAccept(v -> {
                    // Apply events to the aggregate
                    events.forEach(event -> {
                        if (event instanceof PaiementCreated) {
                            aggregate.apply((PaiementCreated) event);
                            paiementEventHandler.handle((PaiementCreated) event);
                        }
                    });
                });
    }
    public List<Paiement> getAllPaiements() {
        return paiementRepo.findAll();
    }

    public Paiement getPaiementById(String id) {
        return paiementRepo.findById(id).orElse(null);
    }

    public CompletableFuture<Void> processLastEvent() {
        return eventStoreRepository.getLastEvent(streamName)
                .thenCompose(lastEvent -> {
                    if (lastEvent instanceof IndemnisationCreated) {
                        IndemnisationCreated indemnisationCreated = (IndemnisationCreated) lastEvent;

                        // Create a new PaiementCreateCommand based on the IndemnisationCreated event
                        PaiementCreateCommand paiementCommand = new PaiementCreateCommand(
                                UUID.randomUUID(), // Generate a unique UUID for the new payment
                                indemnisationCreated.getRef().toString(), // Use the indemnisation ID as description
                                indemnisationCreated.getAmount(), // Same montant as indemnisation
                                LocalDate.now(), // Current date
                                StatusEnum.IN_PROGRESS // Set status to IN_PROGRESS
                        );

                        System.out.println("Creating paiement with command: " + paiementCommand);

                        // Call the paiement service to create the new PaiementCreated event
                        try {
                            return createPaiement(paiementCommand);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // If the last event is not IndemnisationCreated, do nothing
                        return CompletableFuture.completedFuture(null);
                    }
                });
    }
}
