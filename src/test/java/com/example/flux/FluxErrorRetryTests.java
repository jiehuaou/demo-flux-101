package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
public class FluxErrorRetryTests {

    static int counter = 0;

    static String process(Integer integer) {
        if (integer == 5) {
            throw new IllegalArgumentException("test exception");
        }
        return "Number: " + integer;
    }

    static String processConditional(Integer integer) {
        if (integer == 5 && counter<1) {
            counter++;
            throw new IllegalArgumentException("test exception");
        }
        return "Number: " + integer;
    }


    @Test
    void testErrorRetry(){
        Flux.just( 1, 2, 3, 4, 5, 6)
                .doOnNext(x -> log.info("next1 -> {}", x))
                .map(x -> x+1)
                .doOnNext(x -> log.info("next2 -> {}", x))
                .map(FluxErrorRetryTests::process)
                .doOnError(System.err::println)
//                 .onErrorReturn("error XXX")                         // with onErrorXXX, retry will not happen
//                 .onErrorResume((e)->Flux.just("error XXX"))         // with onErrorXXX, retry will not happen
                .retry(3)
                .map(x -> x+1)
                .doOnNext(x -> log.info("next3 -> {}", x))
                .onErrorReturn("error XXX")                           // handle the last error after retry
                .blockLast();
                //.subscribe(e -> log.info("sub -> {}", e));

        log.info("test end");
    }

    @Test
    void testErrorRetryConditional(){
        Flux.just( 1, 2, 3, 4, 5, 6)
                .doOnNext(x -> log.info("next1 -> {}", x))
                .map(x -> x+1)
                .doOnNext(x -> log.info("next2 -> {}", x))
                .map(FluxErrorRetryTests::processConditional)
                .doOnError(System.err::println)
//                 .onErrorReturn("error XXX")                         // with onErrorXXX, retry will not happen
//                 .onErrorResume((e)->Flux.just("error XXX"))         // with onErrorXXX, retry will not happen
                .retry(3)
                .map(x -> x+1)
                .doOnNext(x -> log.info("next3 -> {}", x))
                //.onErrorReturn("error XXX") // handle the last error after retry
                .blockLast();
        //.subscribe(e -> log.info("sub -> {}", e));

        log.info("test end");
    }

}
