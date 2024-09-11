package com.example.demo.entities;

import com.example.demo.event.PrestationCreated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Prestations")
public class Prestation {
    @Id
    private String id; // Identifiant unique de la prestation
    private String description; // Description de la prestation
    private BigDecimal montant; // Montant de la prestation
    private LocalDate date; // Date de la prestation

    public void on(PrestationCreated event) {
        // Appliquer l'événement pour mettre à jour l'état de la prestation
        setId(event.getId().toString());
        setDescription(event.getDescription());
        setMontant(event.getMontant());
        setDate(event.getDate());
    }
}
