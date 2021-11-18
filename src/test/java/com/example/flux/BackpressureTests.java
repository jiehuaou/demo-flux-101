package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

import static com.example.flux.Task.sleeping;
import static com.example.flux.Task.waiting;

@Log4j2
public class BackpressureTests {
    /**
     * consumer with request(N)
     */
    @Test
    void testBackpressure1(){

        Flux.range(1,20)
                .subscribe(new Subscriber<Integer>() {
                    private Subscription s;
                    int counter=0;
                    @Override
                    public void onSubscribe(Subscription s) {
                        System.out.println("onSubscribe");
                        this.s = s;
                        System.out.println("Requesting 2 emissions");
                        s.request(2);
                    }
                    @Override
                    public void onNext(Integer i) {
                        System.out.println("onNext " + i);
                        counter++;
                        if (counter % 2 == 0) {
                             System.out.println("Requesting 2 emissions");
                             s.request(2);
                            // s.cancel();
                        }
                    }
                    @Override
                    public void onError(Throwable t) {
                        System.err.println("onError");
                    }
                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });
    }

    /**
     * park up to maxSize elements when not enough demand is requested downstream.
     * without onBackpressureBuffer() --> "OverflowException: Could not emit tick NNN due to lack of requests"
     */
    @Test
    void testParallelWithBackpressureBuffer(){
        System.out.println("Main thread: " + Thread.currentThread());
        Flux<String> tick = Flux.interval(Duration.ofMillis(10))
                .onBackpressureBuffer(10) // backpressure strategy
                .flatMap(i-> Mono.fromCallable(()->{
                            System.out.println("simulate IO " + Thread.currentThread() + "  " + i);
                            sleeping(1000L); // simulate IO delay, very slow
                            return String.format("String %d", i);
                        }).subscribeOn(Schedulers.boundedElastic())
                        , 3)
                .take(10);

        Disposable disposable = tick.subscribe(x ->System.out.println("Subscribe thread: " + Thread.currentThread() + "  --> " + x),
                System.out::println,
                ()-> System.out.println("Done"));

        waiting(disposable);
        System.out.println("DONE AND DONE");

    }

    /**
     * simulate slow IO delay,
     */
    public static Mono<String> asyncTask(Integer i) {
        return Mono.fromCallable(()->{
            System.out.println("simulate IO " + Thread.currentThread() + " -- " + i);
            sleeping(1000L); // simulate IO delay, very slow
            return String.format("Text %d", i);
        });
    }



    /**
     * limit rate ( prefetchRate )
     *      -> first request ( prefetchRate such as 100 )
     *      -> second request ( prefetchRate * 75% = 75 )
     *      -> and so on
     */
    @Test
    void testLimitRate(){
        Flux.range(1,250)
                .log()
                .limitRate(100)
                //.flatMap(x->asyncTask(x).subscribeOn(Schedulers.boundedElastic()), 5)
                // .doOnNext(x->log.info(x))
                .blockLast();
    }

    /**
     * drop event when overflow occur.
     * you can see drop event after some time.
     */
    @Test
    void testDropEventOnTooMuch(){

        Disposable disposable = Flux.interval(Duration.ofMillis(1))
                .onBackpressureDrop(x->log.warn(" drop event ========> {}", x))
                .flatMap(x->asyncTask(x.intValue()).subscribeOn(Schedulers.boundedElastic()))
                .subscribe();

        waiting(disposable, 1000 * 3);
    }

    /**
     * repeat async task 10 times
     */
    @Test
    void testRepeatTask(){

        Disposable disposable = Mono.defer(()->asyncTask(1))
                .delayElement(Duration.ofMillis(100))
                .repeat(10)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(s->log.info(s));

        waiting(disposable);
    }


}
