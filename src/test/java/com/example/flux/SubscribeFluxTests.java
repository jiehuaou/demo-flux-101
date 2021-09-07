package com.example.flux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.time.Duration;

@SpringBootTest
public class SubscribeFluxTests {
    @Test
    void testSub1(){
        Flux.range(1, 10).delayElements(Duration.ofSeconds(1))
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .blockLast();

        System.out.println("Below chain");
    }
}
