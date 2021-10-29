package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static com.example.flux.Task.*;
import static com.example.flux.Task.singleTaskSchedule;

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
                .thenEmpty(singleTaskSchedule("A").then())
                .thenEmpty(singleTaskSchedule("B").then())
                .thenEmpty(singleTaskSchedule("C").then())
                .thenEmpty(singleTaskSchedule("D").then())
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
        Mono<String> mo = singleTaskSchedule("A");

        Flux<String> ret = mo.thenMany(
                Flux.merge(
                        singleTaskSchedule("B"),
                        singleTaskSchedule("C")
                )
        );

        Disposable disposable = ret.subscribe(s->log.info(s));
        waiting(disposable);

    }

    @Test
    void testMonoThenMany2(){
        Mono<String> mo = singleTaskSchedule("A");

        Flux<String> ret = mo.thenMany(
                multiTaskSchedule("B", "C")
        );

        Disposable disposable = ret.subscribe(s->log.info(s));
        waiting(disposable);

    }

}
