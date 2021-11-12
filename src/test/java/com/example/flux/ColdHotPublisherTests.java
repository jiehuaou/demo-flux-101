package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
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

    /**
     * cold publisher can be subscribed multi-times.
     */
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

    /**
     * subscribe hot publisher in the middle, may miss some elements
     */
    @Test
    void testHotFluxMiddle(){
        Flux<String> cold = Flux
                .fromStream(()->{
                    log.info("cold Flux.fromStream");
                    return Stream.of("a1", "a2", "a3", "a4", "a5");
                })
                .delayElements(Duration.ofMillis(100))
                .share(); //   <-- makes cold source into hot.

        Disposable d0 = cold.subscribe(s -> log.info("user1 watching "+s));

        Task.sleeping(250); // in middle of first subscribe

        Disposable d1 = cold.subscribe(s -> log.info("user2 watching "+s));  // this subscriber might miss items.

        Task.waiting(d0);
        Task.waiting(d1);
    }

    /**
     * subscribe hot publisher after completion of other, will repeat everything.
     */
    @Test
    void testHotFluxCompleted(){
        Flux<String> hot = Flux
                .fromStream(()->{
                    log.info("cold Flux.fromStream");
                    return Stream.of("a1", "a2", "a3", "a4", "a5");
                })
                .delayElements(Duration.ofMillis(100))
                .share(); //   <-- makes cold source into hot.

        Disposable d0 = hot.subscribe(s -> log.info("user1 watching "+s));

        Task.sleeping(600); // after completion of first subscribe

        Disposable d1 = hot.subscribe(s -> log.info("user2 watching "+s));  // this subscriber repeat everything.

        Task.waiting(d0);
        Task.waiting(d1);
    }

    /**
     * with cache of cold publisher, second subscriber is able to watch all the scenes.
     */
    @Test
    void testHotFluxCache(){
        Flux<String> hot = Flux
                .fromStream(()->{
                    log.info("cold Flux.fromStream ----> started ");
                    return Stream.of("a1", "a2", "a3", "a4", "a5");
                })
                .delayElements(Duration.ofMillis(1000))
                .cache(); //   <-- makes cold source into hot.

        Disposable d0 = hot
                .doOnNext(s -> log.info("user1 watching "+s))
                .count().doOnNext(s->log.info("user1 done .........{}", s))
                .subscribe();

        Task.sleeping(1500); // at middle of first subscribe
        Disposable d1 = hot
                .delayElements(Duration.ofMillis(1300))
                .doOnNext(s -> log.info("user2 watching "+s))
                .count().doOnNext(s->log.info("user2 done .........{}", s))
                .subscribe();        // this subscriber repeat everything.

        Task.sleeping(2500); // at middle of second subscribe
        Disposable d2 = hot
                .delayElements(Duration.ofMillis(1500))
                .doOnNext(s -> log.info("user3 watching "+s))
                .count().doOnNext(s->log.info("user3 done .........{}", s))
                .subscribe();       // this subscriber repeat everything.

        Task.waiting(d0, d1, d2);

    }

    @Test
    void testHotFluxPublish(){
        ConnectableFlux<String> hotConnectable = Flux
                .fromStream(()->{
                    log.info("cold Flux.fromStream ----> started ");
                    return Stream.of("a1", "a2", "a3", "a4", "a5");
                })
                .delayElements(Duration.ofMillis(1000))
                .publish();

        Disposable d0 = hotConnectable
                .doOnNext(s -> log.info("user1 watching "+s))
                .count().doOnNext(s->log.info("user1 done .........{}", s))
                .subscribe();

        Task.sleeping(1500); // at middle of first subscribe
        Disposable d1 = hotConnectable
                .delayElements(Duration.ofMillis(1300))
                .doOnNext(s -> log.info("user2 watching "+s))
                .count().doOnNext(s->log.info("user2 done .........{}", s))
                .subscribe();

        // start to consume
        hotConnectable.connect();

        Task.waiting(d0,d1);
    }

}








