package com.example.demo.event;

import events.dewdrop.aggregate.annotation.AggregateId;
import events.dewdrop.structure.api.Event;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class IndemnisationEvent extends Event {
    @AggregateId
    protected UUID ref;

    public IndemnisationEvent(UUID ref) {
        this.ref = ref;
    }
}
