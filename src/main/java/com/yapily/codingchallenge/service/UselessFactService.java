package com.yapily.codingchallenge.service;

import com.yapily.codingchallenge.domain.LangDirections;
import com.yapily.codingchallenge.domain.StatusReport;
import com.yapily.codingchallenge.domain.TranslatedFact;
import com.yapily.codingchallenge.domain.UselessFact;
import com.yapily.codingchallenge.repository.UselessFactRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Api(value = "Useless fact providing service")
public class UselessFactService {

    public static final String URI_QUERY = "?key={key}&lang=en-{lang}&text={text}";
    private final UselessFactRepository repository;
    private final WebClient client;
    @Value("${fact.uri}")
    private String factUri;
    @Value("${translate.uri}")
    private String translateUri;
    @Value("${language.uri}")
    private String getLangUri;
    @Value("${facts.number}")
    private int numberOfFacts;
    @Value("${api.key}")
    private String API_KEY;

    public UselessFactService(UselessFactRepository repository, WebClient client) {
        this.repository = repository;
        this.client = client;
    }

    @ApiOperation(value = "Returns the status", produces = "Status report")
    @GetMapping("/status")
    public Mono<StatusReport> status() {
        Mono<Long> distinctCount = repository.findAll().distinct().count();
        Mono<Long> totalCount = repository.count();
        return distinctCount.zipWith(totalCount).flatMap(tuple2 -> {
            StatusReport.StatusReportBuilder builder = StatusReport.builder()
                    .uniqueFacts(tuple2.getT1())
                    .totalFacts(tuple2.getT2());
            if (tuple2.getT2() < numberOfFacts) {
                builder.status(StatusReport.Status.REPOSITORY_NOT_FULL);
            } else {
                builder.status(StatusReport.Status.COMPLETED);
            }
            return Mono.just(builder.build())
                    .onErrorResume(err -> Mono.just(builder
                            .status(StatusReport.Status.ERROR)
                            .throwable(err)
                            .build()));
        }).onErrorResume(err -> Mono.just(StatusReport.builder().status(StatusReport.Status.ERROR).build()));
    }

    @ApiOperation(value = "Returns ids of all available facts", produces = "List of all fact ids", response = List.class)
    @GetMapping("/facts")
    public Mono<List<String>> getFactIds() {
        return repository.findAll().map(UselessFact::getPermalink)
                .map(link -> URI.create(link).getPath().substring(1))
                .collectList();
    }

    @ApiOperation(value = "Returns all facts", produces = "A list of all facts")
    @GetMapping("/all-facts")
    public Mono<List<UselessFact>> findAll() {
        return repository.findAll().collectList();
    }

    @ApiOperation(value = "Returns a specific fact", consumes = "String representation of a UUID", produces = "A useless fact")
    @GetMapping("/fact/{id}")
    public Mono<UselessFact> findById(@PathVariable("id") String id, @RequestParam("lang") Optional<String> language) {
        final UUID uuid = UUID.fromString(id);
        final Mono<UselessFact> uselessFact = repository.findByFactId(uuid)
                .switchIfEmpty(Mono.error(new RuntimeException("Fact could not be found")));
        if (language.isEmpty()) {
            return uselessFact;
        }
        final String lang = language.get().toLowerCase();
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(translateUri + URI_QUERY);

        Mono<LangDirections> langDirections = getLangDirections();

        return langDirections.flatMap(dir -> {
            if (dir.getLanguagesAvailableForTranslation().contains(lang)) {
                return uselessFact.flatMap(fact -> {
                    String uri = uriBuilder.buildAndExpand(API_KEY, lang, fact.getText()).toUri().toString();
                    Mono<TranslatedFact> translatedFact = getTranslatedFact(uri);
                    return addTranslationDetails(lang, fact, translatedFact);
                });
            } else {
                return uselessFact;
            }
        });
    }

    private Mono<TranslatedFact> getTranslatedFact(String uri) {
        return client.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(TranslatedFact.class);
    }

    private Mono<LangDirections> getLangDirections() {
        return client.get()
                .uri(getLangUri + "?key=" + API_KEY)
                .retrieve()
                .bodyToMono(LangDirections.class);
    }

    private Mono<UselessFact> addTranslationDetails(String lang, UselessFact fact, Mono<TranslatedFact> translation) {
        return translation.map(trans -> {
            fact.setLanguage(lang);
            fact.setText(trans.getText().get(0));
            fact.setTranslated(true);
            return fact;
        })
                .onErrorResume(err -> Mono.just(fact))
                .switchIfEmpty(Mono.error(new RuntimeException()));
    }
}
