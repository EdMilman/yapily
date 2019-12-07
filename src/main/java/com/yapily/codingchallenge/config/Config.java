package com.yapily.codingchallenge.config;

import com.yapily.codingchallenge.domain.UselessFact;
import com.yapily.codingchallenge.repository.UselessFactRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Configuration
public class Config {
    @Value("${facts.number}")
    private int numberOfFacts;
    @Value("${first:false}")
    private String first;
    @Value("${fact.uri}")
    private String factUri;

    @Bean
    CommandLineRunner init(UselessFactRepository repository, WebClient client) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        return args -> {
            if (Boolean.parseBoolean(first)) {
                IntStream.range(0, numberOfFacts).forEach(num ->
                        executorService.submit(() -> client.get()
                                .uri(factUri)
                                .retrieve()
                                .bodyToMono(UselessFact.class)
                                .flatMap(repository::save)
                                .block()));
            }
        };
    }

    @Bean
    WebClient webClient() {
        return WebClient.create();
    }
}
