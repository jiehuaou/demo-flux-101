package com.example.flux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

//@SpringBootTest
public class MonoCallableTests {
    @Test
    void testMonoFromCallable(){

        Mono blockingWrapper = Mono.fromCallable(() -> {
            Task.sleeping(2000L);
            return 20 * 30;
        });
        blockingWrapper.subscribeOn(Schedulers.boundedElastic())
                .map(x-> {
                    System.out.println("Mono -> " + x);
                    return x;
                }).log().block();
    }

    @Test
    void testMonoCreateSink(){

        Mono blockingWrapper = Mono.create(sink -> {
            Task.sleeping(2000L);
            sink.success(123);
            Task.sleeping(2000L);  // will be ignored
            sink.success(456);       // will be ignored
            Task.sleeping(2000L);  // will be ignored
        });
        blockingWrapper.subscribeOn(Schedulers.boundedElastic())
                .map(x-> {
                    System.out.println("map -> " + x);
                    return x;
                }).log().block();
    }

}
