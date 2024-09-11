package com.example.demo.repository;

import com.example.demo.entities.Prestation;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PrestationRepo extends MongoRepository<Prestation, String> {

}
