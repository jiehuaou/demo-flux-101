package com.example.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;


public class Task {
    public static void sleeping(long timeMilli) {
        try {
            Thread.sleep(timeMilli);
        } catch (InterruptedException e) {
            System.out.println("Exiting");
        }
    }

    public static void waiting(Disposable disposable) {
        while (!disposable.isDisposed()){
            System.out.println("...");
            sleeping(1500);
        }
    }

    public static void waiting(Disposable disposable, long expiredMilliSecond) {
        long t1 = System.currentTimeMillis();
        while (!disposable.isDisposed()){
            if((System.currentTimeMillis()-t1)>expiredMilliSecond){
                break;
            }
            System.out.println("...");
            sleeping(1500);
        }
    }

    /**
     * simulate slow IO task,
     */
    public static Mono<String> singleTask(String s) {
        return Mono.fromCallable(()->{
            System.out.println("mock IO " + Thread.currentThread() + " -- " + s);
            sleeping(1000L); // simulate IO task, very slow
            return String.format("Text %s", s);
        });
    }

    /**
     * simulate slow IO task on other schedule
     */
    public static Mono<String> singleTaskSchedule(String s) {
        return Mono
                .defer(()->singleTask(s))
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
