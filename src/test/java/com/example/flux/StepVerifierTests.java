package com.example.flux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


public class StepVerifierTests {

    @Test
    void testExpectNextCount(){

        Flux<Integer> publisher = Flux.just(1,2,3,4);

        StepVerifier
                .create(publisher)
                .expectNext(1,2,3)
                .expectNext(4)
                .expectComplete()
                .verify();

        StepVerifier
                .create(publisher)
                .expectNextCount(4)
                .expectComplete()
                .verify();
    }

    @Test
    void testExpectNext(){

        Mono<Integer> publisher = Mono.just(123);

        StepVerifier
                .create(publisher)
                .expectNext(123)
                .expectComplete()
                .verify();

    }

    @Test
    void testExpectError(){

        Mono<Integer> publisherError = Mono.error(new IllegalArgumentException("something wrong"));

        StepVerifier
                .create(publisherError)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
