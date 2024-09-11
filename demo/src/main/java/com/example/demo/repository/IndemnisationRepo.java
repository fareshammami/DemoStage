package com.example.demo.repository;

import com.example.demo.entities.Indemnisation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface IndemnisationRepo extends MongoRepository<Indemnisation, UUID> {
}
