package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
@Log4j2
public class FluxParallelTests {


//    private static void accept(Integer i)

    private static void doSomething() {
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    void testParallelThenBlock(){
        //CountDownLatch latch = new CountDownLatch(1);


            Flux.range(1, 15)
                .parallel(3)
                .runOn(Schedulers.boundedElastic())
                .doOnNext(i -> {
                    {
                        log.info("task {} -- start", i);
                        doSomething();
                        log.info("task {} *** stop ", i);
                    }
                })
                .sequential()
                .doOnNext(x->log.info("task {} in sequential ", x))
                .blockLast();
    }

    @Test
    void testParallel2ThenBlock(){
        //CountDownLatch latch = new CountDownLatch(1);

        Flux.range(1, 10)
                .flatMap(i -> Mono.fromCallable(()->{
                        log.info("task {} -- start", i);
                        doSomething();
                        log.info("task {} *** stop ", i);
                        return i;
                      }).subscribeOn(Schedulers.boundedElastic())
                , 4)
                //.subscribeOn(Schedulers.boundedElastic())
               // .doOnNext(x->log.info("task {} in sequential ", x))
                .blockLast();
    }


    @Test
    void testParallelThenWait(){
        CountDownLatch latch = new CountDownLatch(1);

        Flux.range(1, 15)
                .parallel(3)
                .runOn(Schedulers.boundedElastic())
                .doOnNext(i -> {
                    {
                        log.info("task {} -- start", i);
                        doSomething();
                        log.info("task {} *** stop ", i);
                    }
                })
                .sequential()
                .doOnNext(System.out::println)
                .doFinally(signal -> latch.countDown())
                .subscribe();

        try {
            while (!latch.await(500, TimeUnit.MILLISECONDS )){
                System.out.println("...");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}
