package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * state could be shared between multi subscribe
 * within side-effect doOnNext(e->{change state})
 */
@Log4j2
public class FluxSideEffectTests {

    static List<String> raw = Arrays.asList("a1", "b2", "a3", "a4", "a5", "a6", "a7", "b2", "b3", "b4", "b5", "b6", "b7");

    /**
     * side-effect doOnNext(lambda) state could be incorrect if return publisher is subscribed twice.
     */
    public static Flux<String> makeData(String label) {
        Random random = new Random();
        int size = 1 + random.nextInt(1000) / 100;
        log.info("random[label:{}] --> {}", label, size);
        AtomicLong counter = new AtomicLong(0);  // this value could be incorrect if makeData() is subscribed twice.
        return Flux
                .defer(()->{
                    Task.sleeping(10);
                    return Flux.fromIterable(raw.subList(0, size ));
                })
                .subscribeOn(Schedulers.newParallel(label))
                .doOnNext(e-> {
                    long state = counter.incrementAndGet();
                    log.info("internal count[label:{}, szie:{}] +1 --> {}", label, size, state);
//                    log.info("task[{}] --> {}", size, e);
                })
                .doOnComplete(()->log.info("totalcount[label:{}, szie:{}] --> {}", label, size, counter.get()));
    }

    /**
     * state is correct if makeData() subscribe once.
     */
    @Test
    void testSingleSubscribe(){
        Flux<String> data = Flux.merge(
                makeData("XXX"),
                makeData("YYY")
        );

        Disposable disposable = data
                .publishOn(Schedulers.boundedElastic())
                //.filter(s->s.charAt(0)=='a')
                .doOnComplete(()->log.info("completed"))
                .subscribe(s->log.info("element --> {}", s));

        Task.waiting(disposable);
    }

    /**
     * doOnNext(e->{ state++ })
     * if makeData("XXX") is subscribed twice, this could make state add-up twice.
     */
    @Test
    void testTwiceSingleSubscribe(){
        Flux<String> data = makeData("XXX");

        Disposable disposable1 = data  // first subscribe
                .publishOn(Schedulers.boundedElastic())
                .filter(s->s.charAt(0)=='a')
                .doOnComplete(()->log.info("completed[a]"))
                .subscribe(s->log.info("element[a] --> {}", s));

        Disposable disposable2 = data   // second subscribe
                .publishOn(Schedulers.boundedElastic())
                .filter(s->s.charAt(0)=='b')
                .doOnComplete(()->log.info("completed[b]"))
                .subscribe(s->log.info("element[b] --> {}", s));

        Task.waiting(disposable1);
        Task.waiting(disposable2);
    }

    /**
     * 1. like   doOnNext(), delayUntil(e->publisher) does not change the element;
     * 2. unlike doOnNext(), delayUntil(e->publisher) will wait until the publisher finished or exception;
     */
    @Test
    void testFluxDelayUntil() {

        Flux.just("1","2")
                .delayUntil(e->Task.simpleMono(e))     // do not change the stream element
                .doOnNext(s->log.info("next {}", s))
                .doFinally(f->log.info("do finally"))
                .blockLast();

        log.info("--- test end ---");
    }

    @Test
    void testMonoDelayUntil() {
        Task.simpleMono("hello")
                .delayUntil(e->Task.simpleMono(e+"-ex1"))   // do not change the stream element
                .delayUntil(e->Task.simpleMono(e+"-ex2"))   // do not change the stream element
                //.delayUntil(e->simple(e+"-error"))
                .doOnNext(s->log.info("next {}", s))
                .doFinally(f->log.info("do finally"))
                .block();

        log.info("--- test end ---");
    }

    /**
     * delayUntil(e->publisher) Error could propagate immediately to downstream.
     */
    @Test
    void testMonoDelayUntilException() {
        Task.simpleMono("hello")
                .delayUntil(e->Task.simpleMono(e+"-ex1"))
                .delayUntil(e->Task.simpleMono(e+"-ex2"))
                .delayUntil(e->Task.simpleMono(e+"-error"))  // this could cause abort
                .doOnNext(s->log.info("next {}", s))
                .doFinally(f->log.info("do finally"))
                .block();

        log.info("--- test end ---");
    }

}
