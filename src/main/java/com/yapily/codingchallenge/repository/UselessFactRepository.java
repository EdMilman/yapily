package com.yapily.codingchallenge.repository;

import com.yapily.codingchallenge.domain.UselessFact;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UselessFactRepository extends ReactiveMongoRepository<UselessFact, String> {
    Mono<UselessFact> findByFactId(UUID id);
}
