package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Log4j2
public class MonoDeferTests {

    public static Mono<String> simple(String s) {

        log.info("mock IO " + Thread.currentThread() + " -- " + s);
        Task.sleeping(1000L); // simulate IO task, very slow
        return Mono.just(String.format("Text %s", s));

    }

    /**
     * simple task will be executed only once even we subscribe twice.
     */
    @Test
    void testMono(){
        Mono<String> task = simple("hello");

        task.subscribeOn(Schedulers.boundedElastic())
                .doOnNext(s->log.info(s))
                .block();

        Task.sleeping(2000);

        task.subscribeOn(Schedulers.boundedElastic())
                .doOnNext(s->log.info(s))
                .block();
    }

    /**
     * defer task will be executed twice here.
     */
    @Test
    void testMonoDefer(){

        Mono<String> task = Task.singleTaskDefer("hello");

        task
                .doOnNext(s->log.info(s))
                .block();

        Task.sleeping(2000);

        task
                .doOnNext(s->log.info(s))
                .block();
    }

    /**
     * defer() can be used in switchIfEmpty() to avoid executing in advance.
     */
    @Test
    void testDeferSwitchEmpty(){
        simple("hello")
                .switchIfEmpty(Mono.defer(()->simple("not-found"))) // this simple() not happen.
                .doFinally(f->log.info("do finally"))
                .block();
    }
    @Test
    void testSwitchEmpty(){
        simple("hello")
                .switchIfEmpty(simple("not-found"))   // this simple() always happen even if publisher has data.
                .doFinally(f->log.info("do finally"))
                .block();
    }

    public static Mono<String> emptyMono() {
        return Mono.empty();
    }
}
