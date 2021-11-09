package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static com.example.flux.Task.*;
import static com.example.flux.Task.singleTaskDefer;

/**
 * converting between Mono and Flux
 */
@Log4j2
public class ConvertingTests {

    /**
     * convert Mono<List<String>> to Flux<String>
     *
     */
    @Test
    void testConvert(){
        List<String> data = Arrays.asList("111", "222");

        Mono<List<String>> ls = Mono.just(data);

        Flux<String> fs = ls.flatMapMany(ss ->Flux.fromIterable(ss));

        fs.subscribe(s->log.info(s));
    }

    /**
     * doOnNext() never happen after then()
     */
    @Test
    void testMonoThen(){
        Mono<String> mo = Mono.just("hello");
        mo.then()
                .doOnNext(x->log.info(x)) // never happen here
                .doFinally(x->log.info("---end---"))
                .subscribe();

    }
    /**
     * X.thenEmpty(A).thenEmpty(B) represents concatenation of the source completion
     * X completed -> A completed -> B completed
     * all return void
     */
    @Test
    void testMonoThenEmpty(){
        Mono<String> mo = Mono.just("hello");
        Disposable disposable = mo
                .thenEmpty(singleTaskDefer("A").then())
                .thenEmpty(singleTaskDefer("B").then())
                .thenEmpty(singleTaskDefer("C").then())
                .thenEmpty(singleTaskDefer("D").then())
                .doOnNext(x->log.info(x)) // never happen here
                .doFinally(x->log.info("---end---"))
                //.subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        waiting(disposable);
    }

    /**
     * MonoTask("A").thenMany( Flux.merge( MonoTask("B"), MonoTask("C") ) )
     * when A completed, then run B and C, until B and C completed.
     * return the merge of B and C.
     */
    @Test
    void testMonoThenMany1(){
        Mono<String> mo = singleTaskDefer("A");

        Flux<String> ret = mo.thenMany(
                Flux.merge(
                        singleTaskDefer("B"),
                        singleTaskDefer("C")
                )
        );

        Disposable disposable = ret.subscribe(s->log.info(s));
        waiting(disposable);

    }

    @Test
    void testMonoThenMany2(){
        Mono<String> mo = singleTaskDefer("A");

        Flux<String> ret = mo.thenMany(
                multiTaskDefer("B", "C")
        );

        Disposable disposable = ret.subscribe(s->log.info(s));
        waiting(disposable);

    }

    /**
     * converting: Flux(multi-value) -> Mono
     */
    @Test
    void testFlux2Mono(){
        Flux<String> flux = Flux.just("A", "B", "C");

        Mono first = flux.next();
        first.subscribe(s->log.info("first --> {}", s));

        Mono last = flux.last();
        last.subscribe(s->log.info("last --> {}", s));

        Mono single = flux.next().switchIfEmpty(Mono.just("not-found"));
        single.subscribe(s->log.info("single --> {}", s));
    }

    /**
     * converting: Flux(single-value) -> Mono
     */
    @Test
    void testFlux2Mono2(){
        Flux<String> flux = Flux.just("A");
        // flux contain single element
        Mono single = flux.single("default if not-found");
        single.subscribe(s->log.info("single --> {}", s));
    }

    /**
     * converting: Flux(value) -> Flux(value1, value2)
     */
    @Test
    void testFlux2Flux(){
        Flux<String> flux = Flux.just("A", "B");
        flux
                .flatMap(x->Flux.just(x + "1", x + "2"))
                .subscribe(x->log.info(x));
    }



}
