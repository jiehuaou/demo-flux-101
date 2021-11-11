package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Stream;

/**
 * hot publisher:
 * 1) do not create new data producer for each new subscription,
 * 2) Instead there will be only one data producer and all the observers listen to the data produced by the single data producer.
 * 3) So all the observers get the same data.
 *
 * cold publisher:
 * 1) by default do not produce any value unless at least 1 observer subscribes to it.
 * 2) Publishers create new data producers for each new subscription.
 *
 */
@Log4j2
public class ColdHotPublisherTests {

    public static Mono<String> coldMonoSupplier() {
        return Mono.fromSupplier(()->{
            log.info("cold Mono.fromSupplier");
            return "hello";
        });
    }

    public static Mono<String> coldMonoDefer() {
        return Mono.defer(()->{
            log.info("cold Mono.defer");
            return Mono.just("hello");
        });
    }

    public static Mono<String> coldMonoCallable() {
        return Mono.fromCallable(()->{
            log.info("cold Mono.fromCallable");
            return "hello";
        });
    }

    public static Flux<String> coldFluxStream() {
        return Flux.fromStream(()->{
            log.info("cold Flux.fromStream");
            return Stream.of("a1", "a2", "a3");
        });
    }

    @Test
    void testColdMono(){
        Mono<String> cold1 = coldMonoSupplier();
        cold1.subscribe(s -> log.info(s));
        cold1.subscribe(s -> log.info(s));

        Mono<String> cold2 = coldMonoDefer();
        cold2.subscribe(s -> log.info(s));
        cold2.subscribe(s -> log.info(s));

        Mono<String> cold3 = coldMonoCallable();
        cold3.subscribe(s -> log.info(s));
        cold3.subscribe(s -> log.info(s));
    }

    @Test
    void testColdFlux(){
        Flux<String> cold = coldFluxStream();
        cold.subscribe(s -> log.info(s));
        cold.subscribe(s -> log.info(s));
    }

    @Test
    void testHotFlux(){
        Flux<String> cold = Flux
                .fromStream(()->{
                    log.info("cold Flux.fromStream");
                    return Stream.of("a1", "a2", "a3", "a4", "a5");
                })
                .delayElements(Duration.ofMillis(100))
                .share(); //   <-- makes cold source into hot.

        Disposable d0 = cold.subscribe(s -> log.info("user1 watching "+s));

        Task.sleeping(250);
        Disposable d1 = cold.subscribe(s -> log.info("user2 watching "+s));  // this subscriber might miss items.

        Task.waiting(d0);
        Task.waiting(d1);
    }
}
