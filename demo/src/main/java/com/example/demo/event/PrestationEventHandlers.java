package com.example.demo.event;

import com.example.demo.entities.Prestation;
import com.example.demo.repository.PrestationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrestationEventHandlers {

    @Autowired
    private PrestationRepo prestationRepo;

    public void handle(PrestationCreated event) {
        Prestation prestation = new Prestation();
        prestation.setId(event.getId().toString());
        prestation.setDescription(event.getDescription());
        prestation.setMontant(event.getMontant());
        prestation.setDate(event.getDate());
        prestationRepo.save(prestation);
    }
}
   