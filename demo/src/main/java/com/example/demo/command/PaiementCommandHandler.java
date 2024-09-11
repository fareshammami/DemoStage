package com.example.demo.command;


import com.example.demo.command.PaiementCreateCommand;
import com.example.demo.aggregate.PaiementAggregate;
import com.example.demo.event.PaiementCreated;
import com.example.demo.configs.EventStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PaiementCommandHandler {

    @Autowired
    private EventStoreRepository eventStoreRepository;

    public CompletableFuture<Void> handleCreateCommand(PaiementCreateCommand command) throws Exception {
        PaiementAggregate aggregate = new PaiementAggregate();
        List<Object> events = aggregate.handle(command);

        // Save events to EventStore
        return eventStoreRepository.save(command.getId(), events)
                .thenAccept(v -> {
                    // Apply events to the aggregate
                    events.forEach(event -> {
                        if (event instanceof PaiementCreated) {
                            aggregate.apply((PaiementCreated) event);
                        }
                    });
                });
    }
}