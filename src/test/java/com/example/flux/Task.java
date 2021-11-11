package com.example.flux;

import lombok.extern.log4j.Log4j2;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;


@Log4j2
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

    public static void waiting(Disposable ...disposable) {
        int len = disposable.length;
        for (int i = 0; i < len; i++) {
            Disposable d = disposable[i];
            waiting(d);
            //System.out.printf("disp[%d]...closed \n", i);
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
     * simulate slow IO task,<br>
     * <pre>
     * <code>
     * Mono<String> task = simpleTask("hello")
     * </code></pre>
     * <br>
     * this task will be executed immediately/eagerly
     *
     */
//    public static Mono<String> simpleTask(String s) {
//        log.info("mock IO " + Thread.currentThread() + " -- " + s);
//        Task.sleeping(1000L); // simulate IO task, very slow
//        return Mono.just(String.format("Text %s", s));
//    }

    /**
     * simulate slow IO task,<br>
     *
     * this task will be executed lazily like Mono.defer(), but normally do some our own implementing.
     *
     */
    public static Mono<String> singleTask(String s) {
        return Mono.fromCallable(()->{
            log.info("mock IO " + Thread.currentThread() + " -- " + s);
            sleeping(1000L); // simulate IO task, very slow
            return String.format("Text %s", s);
        });
    }

    /**
     * simulate slow IO task on other schedule
     *
     * <pre>
     * <code>
     *
     * Mono<String> task = singleTaskDefer("hello")
     * </code>
     * </pre>
     * this task will be executed only when we subscribe it,
     * like <code> Mono.subscribe() </code>
     * <br>
     * this task will be executed lazily like Mono.fromCallable(), but normally invoke other lib API.
     */
    public static Mono<String> singleTaskDefer(String s) {
        return Mono
                .defer(()->singleTask(s))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * simulate slow IO task on other schedule
     */
    public static Flux<String> multiTaskDefer(String... ss) {
        return Flux
                .defer(()->Flux.fromArray(ss))
                .doOnNext(s->{
                    System.out.println("mock IO " + Thread.currentThread() + " -- " + s);
                })
                .delayElements(Duration.ofMillis(100))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * simple Mono
     */
    public static Mono<String> simpleMono(String s) {

        log.info("mock IO " + Thread.currentThread() + " -- " + s + "  happening");
        Task.sleeping(500L); // simulate IO task, very slow
        if(s.contains("error")){
            throw new RuntimeException("Foo Error");
        }
        return Mono.just(String.format("Text %s", s));

    }
}
