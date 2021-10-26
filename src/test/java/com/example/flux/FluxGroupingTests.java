package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

/**
 * flux -> buffer(), groupBy()
 */
@Log4j2
public class FluxGroupingTests {
    @Test
    void testBuffer(){
        Flux<String> flux = Flux.just("a1", "b1", "c1", "a2", "b2", "c2", "a7", "a8", "a9", "a10");

        flux
                .buffer(5)
                .doOnNext(x->log.info(x))
                .subscribe();
    }
    @Test
    void testGroupBy(){
        Flux<String> flux = Flux.just("a1", "b1", "c1", "a2", "b2", "c2", "a7", "a8", "a9", "a10");

        flux
                .groupBy(x->x.charAt(0))
                .flatMap(x->x.buffer())
//                .concatMap(groupedFlux -> groupedFlux.startWith("Group " + groupedFlux.key()))
                .doOnNext(x->log.info(x))
                .subscribe();
    }
}
