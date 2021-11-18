package com.example.flux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

/**
 * Mono.fromCallable() - Create a Mono producing its value using the provided Callable
 * Mono.defer() - this task will be executed lazily like Mono.fromCallable(), but normally invoke other lib API.
 * Mono.create() - the most advanced method that gives you the full control over the emitted values.
 */
public class MonoCallableCreatorTests {
    @Test
    void testMonoFromCallable(){

        Mono publisher = Mono.fromCallable(() -> {
            Task.sleeping(2000L);
            return 20 * 30;
        });
        publisher.subscribeOn(Schedulers.boundedElastic())
                .map(x-> {
                    System.out.println("Mono -> " + x);
                    return x;
                }).log().block();
    }

    /**
     * Mono.create() :
     * the most advanced method that gives you the full control over the emitted values.
     */
    @Test
    void testMonoCreateSink(){

        Mono publisher = monoCreator(123);

        StepVerifier
                .create(publisher)
                .expectNext(123)
                .expectComplete()
                .verify();

        Mono publisherError = monoCreator(-1);
        StepVerifier
                .create(publisherError)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    public static Mono<Integer> monoCreator(int value) {
        Mono<Integer> publisher = Mono.create(sink -> {
            Task.sleeping(100L);
            if(value>0) {
                sink.success(value);
            }else {
                sink.error(new IllegalArgumentException("invalid arg: " + value));
            }
            Task.sleeping(2000L);  // will be ignored
            sink.success(456);       // will be ignored
            Task.sleeping(2000L);  // will be ignored
        });

        return publisher;
    }

    @Test
    void testMonoOptional(){
        Mono<String> data1 = Mono.just("123");
        Mono<String> data2 = Mono.justOrEmpty("123");
        Mono<String> data3 = Mono.justOrEmpty(null);

        // Mono<String> data4 = Mono.just(null);  // will cause NullPointerException

        StepVerifier
                .create(data1)
                .expectNext("123")
                .expectComplete()
                .verify();
        StepVerifier
                .create(data2)
                .expectNext("123")
                .expectComplete()
                .verify();
        StepVerifier
                .create(data3)
                .expectComplete()
                .verify();
    }

}
