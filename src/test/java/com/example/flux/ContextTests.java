package com.example.flux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class ContextTests {
    @Test
    void testContext() {

        String key = "key";
        Mono<String> mono = Mono.just("anything")
                .flatMap(s -> Mono.deferContextual(Mono::just).map(ctx -> "Value stored in context: " + ctx.get(key)))
                .subscriberContext(ctx -> ctx.put(key, "myValue"));

        StepVerifier.create(mono)
                .expectNext("Value stored in context: myValue")
                .verifyComplete();

    }

    @Test
    void testContext2() {

        String key = "key";
        Mono<String> mono = Mono.just("anything")
                .flatMap(s -> Mono.subscriberContext().map(ctx -> "Value stored in context: " + ctx.get(key)))
                .deferContextual(ctx -> Mono.just("myValue"));

        mono.log().block();

    }
}