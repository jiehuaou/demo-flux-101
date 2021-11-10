package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Log4j2
public class MonoDeferTests {


    /**
     * simple task will be executed only once even we subscribe twice.
     */
    @Test
    void testMono(){
        Mono<String> task = Task.simpleMono("hello");

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
        Task.simpleMono("hello")
                .switchIfEmpty(Mono.defer(()->Task.simpleMono("not-found"))) // this simple() not happen.
                .doOnNext(s->log.info("next {}", s))
                .doFinally(f->log.info("do finally"))
                .block();
    }
    @Test
    void testSwitchEmpty(){
        Task.simpleMono("hello")
                .switchIfEmpty(Task.simpleMono("not-found"))   // this simple() always happen even if publisher has data.
                .doOnNext(s->log.info("next {}", s))
                .doFinally(f->log.info("do finally"))
                .block();
    }

}








