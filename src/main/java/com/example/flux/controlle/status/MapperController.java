package com.example.flux.controlle.status;

import com.example.flux.model.ErrorBody;
import com.example.flux.model.Hello;
import com.example.flux.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * use onStatus to monitor the error text when status 4XX,5XX
 */
@Slf4j
@RestController
public class MapperController {

    @Autowired
    WebClient.Builder builder;

    @RequestMapping(value = "/map/sub/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<Student>> getStudent(@PathVariable String id) {
        log.info("GET: /sub/{} -----> invoked", id);

        Mono<Student> objectMono = builder.baseUrl("http://localhost:8080").build()
                .get()
                .uri("/sub/" + id)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .doOnNext(body ->
                                    log.error("clientResponse with {}", body))
                            .flatMap(e -> clientResponse.createException());

                })
                .bodyToMono(Student.class)
                .doOnSuccess(student -> log.info("success call with {}", student));

        return ResponseEntity.ok(objectMono);
    }
}
