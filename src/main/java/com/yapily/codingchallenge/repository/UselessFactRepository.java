package com.yapily.codingchallenge.repository;

import com.yapily.codingchallenge.domain.UselessFact;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UselessFactRepository extends ReactiveMongoRepository<UselessFact, UUID> {
}
