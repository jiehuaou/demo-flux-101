package com.example.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;


public class Helper {
    public static void sleep(long timeMilli) {
        try {
            Thread.sleep(timeMilli);
        } catch (InterruptedException e) {
            System.out.println("Exiting");
        }
    }

    public static void waiting(Disposable disposable) {
        while (!disposable.isDisposed()){
            System.out.println("...");
            sleep(1500);
        }
    }

    /**
     * simulate slow IO task,
     */
    public static Mono<String> mockTask(String s) {
        return Mono.fromCallable(()->{
            System.out.println("mock IO " + Thread.currentThread() + " -- " + s);
            sleep(1000L); // simulate IO task, very slow
            return String.format("Text %s", s);
        });
    }

    /**
     * simulate slow IO task on other schedule
     */
    public static Mono<String> mockTaskSchedule(String s) {
        return Mono
                .defer(()->mockTask(s))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * simulate slow IO task on other schedule
     */
    public static Flux<String> multiTaskSchedule(String... ss) {
        return Flux
                .defer(()->Flux.fromArray(ss))
                .doOnNext(s->{
                    System.out.println("mock IO " + Thread.currentThread() + " -- " + s);
                })
                .delayElements(Duration.ofMillis(100))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
