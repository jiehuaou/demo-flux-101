package com.example.flux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

//@SpringBootTest
public class WrappingSynchronousTests {
    @Test
    void testWrappingSynchronous(){
        Mono blockingWrapper = Mono.fromCallable(() -> {
            return 20 * 30;
        });
        blockingWrapper.subscribeOn(Schedulers.boundedElastic())
                .map(x-> {
                    System.out.println("Mono -> " + x);
                    return x;
                }).log().block();
    }
}
