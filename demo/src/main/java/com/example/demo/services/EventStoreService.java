package com.example.demo.services;

import com.eventstore.dbclient.AppendToStreamOptions;
import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.ReadStreamOptions;
import com.example.demo.entities.Event;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class EventStoreService {
    private final EventStoreDBClient eventStoreDBClient;
    private final Gson gson;

    @Autowired
    public EventStoreService(EventStoreDBClient eventStoreDBClient, Gson gson) {
        this.eventStoreDBClient = eventStoreDBClient;
        this.gson = gson;
    }

    public CompletableFuture<Void> addEvent(Event event) {
        String json = gson.toJson(event);
        EventData eventData = EventData.builderAsJson("MyEvent", json.getBytes(StandardCharsets.UTF_8))
                .eventId(UUID.randomUUID())
                .build();

        return eventStoreDBClient.appendToStream("mystream", AppendToStreamOptions.get(), eventData)
                .thenApply(result -> null);
    }

    public CompletableFuture<List<Event>> getEvents() {
        List<Event> events = new ArrayList<>();
        return eventStoreDBClient.readStream("mystream", ReadStreamOptions.get())
                .thenApply(readResult -> {
                    readResult.getEvents().forEach(resolvedEvent -> {
                        String json = new String(resolvedEvent.getOriginalEvent().getEventData(), StandardCharsets.UTF_8);
                            Event event = gson.fromJson(json, Event.class);
                            events.add(event);

                    });
                    return events;
                });
    }

    public CompletableFuture<Event> getLastEvent() {
        return eventStoreDBClient.readStream("mystream", ReadStreamOptions.get().fromEnd().backwards().maxCount(1))
                .thenApply(readResult -> {
                    ResolvedEvent resolvedEvent = readResult.getEvents().get(0);
                    String json = new String(resolvedEvent.getOriginalEvent().getEventData(), StandardCharsets.UTF_8);
                        return gson.fromJson(json, Event.class);

                });
    }
}
