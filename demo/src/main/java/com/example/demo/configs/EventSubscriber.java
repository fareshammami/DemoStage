package com.example.demo.configs;

import com.eventstore.dbclient.*;
import com.example.demo.entities.StatusEnum;
import com.example.demo.event.IndemnisationCreated;
import com.example.demo.event.PaiementCreated;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class EventSubscriber {

    @Autowired
    private EventStoreDBClient eventStore;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String STREAM_NAME = "dewdrop.IndemnisationAggregate-f47ac10b-58cc-4372-a567-0e02b6f3d479";

    @PostConstruct
    public void subscribeToEvents() {
        eventStore.subscribeToStream(
                STREAM_NAME,
                new SubscriptionListener() {
                    @Override
                    public void onEvent(Subscription subscription, ResolvedEvent event) {
                        try {
                            String eventType = event.getOriginalEvent().getEventType();
                            System.out.println("Received event type: " + eventType); // Debugging log

                            if ("IndemnisationCreated".equals(eventType)) {
                                byte[] eventDataBytes = event.getOriginalEvent().getEventData();
                                String eventData = new String(eventDataBytes, StandardCharsets.UTF_8);
                                System.out.println("Event data: " + eventData); // Debugging log

                                IndemnisationCreated indemnisationCreated = objectMapper.readValue(eventData, IndemnisationCreated.class);

                                // Handle the IndemnisationCreated event
                                handleIndemnisationCreated(indemnisationCreated);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(Subscription subscription, Throwable throwable) {
                        throwable.printStackTrace();
                    }
                },
                SubscribeToStreamOptions.get()
        );
    }

    private void handleIndemnisationCreated(IndemnisationCreated event) {
        try {
            PaiementCreated paiementCreated = new PaiementCreated(
                    UUID.randomUUID(), // Unique ID for the payment event
                    event.getRef().toString(), // Convert UUID to String for the reference
                    event.getAmount(), // Use the amount from IndemnisationCreated
                    LocalDate.now(), // Date of the payment creation
                    StatusEnum.IN_PROGRESS // Set status to IN_PROGRESS
            );

            // Save the PaiementCreated event to the specified stream
            savePaiementCreatedEvent(paiementCreated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePaiementCreatedEvent(PaiementCreated event) throws Exception {
        String eventType = event.getClass().getSimpleName();
        String eventJsonData = objectMapper.writeValueAsString(event);
        EventData eventData = EventData.builderAsJson(eventType, eventJsonData.getBytes(StandardCharsets.UTF_8))
                .eventId(UUID.randomUUID())
                .build();

        // Append PaiementCreated event to the specified stream
        System.out.println("Appending to stream: " + STREAM_NAME); // Debugging log

        try {
            eventStore.appendToStream(
                    STREAM_NAME,
                    AppendToStreamOptions.get().expectedRevision(ExpectedRevision.any()), // Use any version for the stream
                    eventData
            ).get();

            System.out.println("PaiementCreated event saved to stream: " + STREAM_NAME); // Debugging log
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
