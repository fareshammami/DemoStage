package com.example.demo.controllers;

import com.example.demo.services.EventStoreService;
import com.example.demo.entities.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/test")
public class EventStoreControlleur {

    private final EventStoreService eventStoreService;

    @Autowired
    public EventStoreControlleur(EventStoreService eventStoreService) {
        this.eventStoreService = eventStoreService;
    }

    @PostMapping
    public CompletableFuture<Void> addEvent(@RequestBody Event event) {
        return eventStoreService.addEvent(event);
    }

    @GetMapping
    public CompletableFuture<List<Event>> getEvents() {
        return eventStoreService.getEvents();
    }

    @GetMapping("/last")
    public CompletableFuture<Event> getLastEvent() {
        return eventStoreService.getLastEvent();
    }
}
