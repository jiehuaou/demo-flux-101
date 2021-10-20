package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Flux: FlatMap vs ConcatMap
 */

@Log4j2
public class FluxFlatMapConcatMapTests {

    /**
     * flatMap - run in multiple thread, element output may be in different order
     * ["a1","a2","a3","a4","a5","a6"] -> ["a3","a1","a2","a6","a5","a4"]
     */
    @Test
    void testFlatMap()  {

        //CountDownLatch countDown = new CountDownLatch(1);

        Flux<String> data = Flux.just("a1","a2","a3","a4","a5","a6");

        data.flatMap(x->Flux.just(x).delayElements(Duration.ofSeconds(1))
                    , 10)
                .doOnNext(x->log.info(x))
                .doFinally((x)->log.info("----- done -----"))
                .blockLast();

    }

    /**
     * concatMap - run in multiple thread, element output must be in original order
     * [1,2,3,4,5,6] -> [1,2,3,4,5,6]
     */
    @Test
    void testConcatMap(){
        Flux<Integer> data = Flux.just(1,2,3,4,5,6);

        data.concatMap(x->Flux.just(x).delayElements(Duration.ofSeconds(6-x)))
                .doOnNext(x->log.info(x))
                .doFinally((x)->log.info("----- done -----"))
                .blockLast();
        log.info("----- test end -----");
    }
}
