package com.example.demo.configs;

import com.eventstore.dbclient.*;
import com.example.demo.event.IndemnisationCreated;
import com.example.demo.event.PaiementCreated;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public class EventStoreRepository {

    @Autowired
    private EventStoreDBClient eventStore;


    @Autowired
    private ObjectMapper objectMapper;

    private final String streamName = "dewdrop.IndemnisationAggregate-f47ac10b-58cc-4372-a567-0e02b6f3d479";

    public EventStoreRepository(EventStoreDBClient eventStore, ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<Void> save(UUID aggregateId, List<Object> events) throws Exception {
        List<CompletableFuture<WriteResult>> futures = new ArrayList<>();

        for (Object event : events) {
            String eventType = event.getClass().getSimpleName();
            String eventJsonData = objectMapper.writeValueAsString(event);
            EventData eventData = EventData.builderAsJson(eventType, eventJsonData.getBytes(StandardCharsets.UTF_8))
                    .eventId(UUID.randomUUID())
                    .build();

            // Use the predefined stream name
            CompletableFuture<WriteResult> future = eventStore.appendToStream(
                    streamName,
                    AppendToStreamOptions.get().expectedRevision(ExpectedRevision.any()), // Change here
                    eventData
            );

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public List<Object> getEvents(UUID aggregateId) throws Exception {
        List<Object> events = new ArrayList<>();
        ReadStreamOptions options = ReadStreamOptions.get().fromStart();
        ReadResult result = eventStore.readStream(aggregateId.toString(), options).get();
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            String eventType = resolvedEvent.getOriginalEvent().getEventType();
            byte[] eventDataBytes = resolvedEvent.getOriginalEvent().getEventData();
            String eventData = new String(eventDataBytes, StandardCharsets.UTF_8); // Convert byte[] to String
            Class<?> eventClass = Class.forName("events.dewdrop.message.event.prestation." + eventType);
            Object event = objectMapper.readValue(eventData, eventClass);
            events.add(event);
        }
        return events;
    }
    public CompletableFuture<IndemnisationCreated> getLastEvent(String streamName) {
        return eventStore.readStream(streamName, ReadStreamOptions.get().fromStart())
                .thenApply(result -> {
                    if (result.getEvents().isEmpty()) {
                        return null; // Return null if there are no events
                    }
                    ResolvedEvent lastEvent = result.getEvents().get(result.getEvents().size() - 1);
                    String eventType = lastEvent.getOriginalEvent().getEventType();
                    String eventData = new String(lastEvent.getOriginalEvent().getEventData(), StandardCharsets.UTF_8);

                    // Log event details
                    System.out.println("Last event type: " + eventType);
                    System.out.println("Last event data: " + eventData);

                    try {
                        if (eventType.equals("IndemnisationCreated")) {
                            return objectMapper.readValue(eventData, IndemnisationCreated.class);
                        } else {
                            return null; // Return null for other event types
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse event data", e);
                    }
                })
                .exceptionally(ex -> {
                    throw new RuntimeException("Error reading last event from stream", ex);
                });
    }
}
