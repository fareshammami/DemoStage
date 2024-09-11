package com.example.demo.event;

import com.example.demo.entities.StatusEnum;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class IndemnisationValidated extends IndemnisationEvent {
    private StatusEnum status;

    public IndemnisationValidated(UUID ref, StatusEnum status) {
        super(ref);
        this.status = status;
    }
}
