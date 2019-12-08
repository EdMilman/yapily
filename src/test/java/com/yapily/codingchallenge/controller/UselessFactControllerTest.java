package com.yapily.codingchallenge.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yapily.codingchallenge.config.Config;
import com.yapily.codingchallenge.domain.LangDirections;
import com.yapily.codingchallenge.domain.StatusReport;
import com.yapily.codingchallenge.domain.TranslatedFact;
import com.yapily.codingchallenge.domain.UselessFact;
import com.yapily.codingchallenge.repository.UselessFactRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(classes = {UselessFactController.class, Config.class})
class UselessFactControllerTest {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static MockWebServer server;
    @MockBean
    private UselessFactRepository repository;
    @Autowired
    private UselessFactController service;

    @BeforeAll
    static void setUp() throws IOException {
        server = new MockWebServer();
        server.start(8081);
    }

    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void shouldReturnStatusReportStatusComplete() {
        UselessFact fact1 = UselessFact.builder().id(UUID.randomUUID()).build();
        UselessFact fact2 = UselessFact.builder().id(UUID.randomUUID()).build();
        when(repository.count()).thenReturn(Mono.just(2L));
        when(repository.findAll()).thenReturn(Flux.just(fact1, fact2));
        StatusReport expected = StatusReport.builder()
                .status(StatusReport.Status.COMPLETED)
                .uniqueFacts(2L)
                .totalFacts(2L)
                .build();
        StepVerifier.create(service.status())
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldReturnStatusReportStatusLoading() {
        UselessFact fact1 = UselessFact.builder().id(UUID.randomUUID()).build();
        when(repository.count()).thenReturn(Mono.just(1L));
        when(repository.findAll()).thenReturn(Flux.just(fact1));
        StatusReport expected = StatusReport.builder()
                .status(StatusReport.Status.REPOSITORY_NOT_FULL)
                .uniqueFacts(1L)
                .totalFacts(1L)
                .build();
        StepVerifier.create(service.status())
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldReturnStatusReportStatusError() {
        UselessFact fact1 = UselessFact.builder().id(UUID.randomUUID()).build();
        when(repository.count()).thenReturn(Mono.error(new RuntimeException()));
        when(repository.findAll()).thenReturn(Flux.just(fact1));
        StatusReport expected = StatusReport.builder()
                .status(StatusReport.Status.ERROR)
                .build();
        StepVerifier.create(service.status())
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldReturnFactId() {
        UselessFact fact = UselessFact.builder()
                .permalink("http://randomuselessfact.appspot.com/ahNzfnJhbmRvbXVzZWxlc3NmYWN" +
                        "0chELEgRGYWN0GICAgICE3uYLDA").build();
        when(repository.findAll()).thenReturn(Flux.just(fact));

        StepVerifier.create(service.getFactIds())
                .expectNext(List.of("ahNzfnJhbmRvbXVzZWxlc3NmYWN0chELEgRGYWN0GICAgICE3uYLDA"))
                .verifyComplete();
    }

    @Test
    void shouldReturnFactInEnglish() {
        UselessFact fact1 = UselessFact.builder().id(UUID.randomUUID()).build();
        when(repository.findById(any(UUID.class))).thenReturn(Mono.just(fact1));

        StepVerifier.create(service.findById(UUID.randomUUID().toString(), Optional.empty()))
                .expectNext(fact1)
                .verifyComplete();
    }

    @Test
    void shouldNotTranslateIfLanguageNotAvailable() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        UselessFact fact1 = UselessFact.builder().id(id).language("en").build();
        when(repository.findById(any(UUID.class))).thenReturn(Mono.just(fact1));

        LangDirections directions = LangDirections.builder().dirs(List.of("en-rs")).build();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(OBJECT_MAPPER.writeValueAsString(directions)));

        StepVerifier.create(service.findById(UUID.randomUUID().toString(), Optional.of("zz")))
                .expectNext(fact1)
                .verifyComplete();
    }

    @Test
    void shouldTranslateIfLanguageAvailable() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        UselessFact fact1 = UselessFact.builder().id(id).language("en").build();
        when(repository.findById(any(UUID.class))).thenReturn(Mono.just(fact1));

        LangDirections directions = LangDirections.builder().dirs(List.of("en-rs")).build();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(OBJECT_MAPPER.writeValueAsString(directions)));

        TranslatedFact translatedFact = TranslatedFact.builder()
                .lang("rs")
                .text(List.of("new text"))
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(OBJECT_MAPPER.writeValueAsString(translatedFact))
        );

        UselessFact expected = UselessFact.builder()
                .id(id)
                .language("rs")
                .text("new text")
                .translated(true)
                .build();

        StepVerifier.create(service.findById(UUID.randomUUID().toString(), Optional.of("rs")))
                .expectNext(expected)
                .verifyComplete();

    }
}