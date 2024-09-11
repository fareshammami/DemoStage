package com.example.demo.aggregate;

import com.example.demo.command.PrestationCreateCommand;
import com.example.demo.event.PrestationCreated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrestationAggregate {

    private UUID id;
    private String description;
    private BigDecimal montant;
    private LocalDate date;

    public List<Object> handle(PrestationCreateCommand command) {
        List<Object> events = new ArrayList<>();
        PrestationCreated prestationCreated = new PrestationCreated(
                command.getId(), command.getDescription(), command.getMontant(), command.getDate()
        );

      //  PrestationCreated.builder().description(command.getDescription()).montant(command.getMontant()).date(command.getDate()).build();

      events.add(prestationCreated);
      apply(prestationCreated);
      return events;
    }

    public void apply(PrestationCreated event) {
        this.id = event.getId();
        this.description = event.getDescription();
        this.montant = event.getMontant();
        this.date = event.getDate();
    }
}