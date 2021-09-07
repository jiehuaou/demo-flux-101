package com.example.flux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@RestController
public class ReactiveController {

    @Autowired
    WebClient.Builder builder;

    @RequestMapping("/say/{name}")
    public ResponseEntity<Mono<Hello>> say(@PathVariable String name){
        log.info("/say/{} -----> invoked", name);
        Mono<Hello> mono = Mono.just(new Hello("hello", name)).log();
        return ResponseEntity.ok(mono);
    }

    @RequestMapping(value = "/hello",  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Hello> helloAll(){
        Flux<Hello> ret = Flux.just(new Hello("hello", "a1"),
                new Hello("hello", "a2"),
                new Hello("hello", "a3")
        ).delayElements(Duration.ofMillis(100)).log();

        return ret;
    }

    @RequestMapping("/say-www")
    public ResponseEntity<Mono<Hello>> sayWWW(){
        Mono<Hello> hello = builder.baseUrl("http://localhost:8080").build()
                .get()
                .uri("/say/www")
                .headers(headers -> headers.setBasicAuth("admin", "123"))
                .retrieve()
                .bodyToMono(Hello.class).log();
        log.info("/say-www  -----> invoked");
        return ResponseEntity.ok(hello);
    }

}
