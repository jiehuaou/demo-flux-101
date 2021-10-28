package com.example.flux;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;


public class BackpressureTests {
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

    @Test
    void testParallelWithBackpressureBuffer(){
        System.out.println("Main thread: " + Thread.currentThread());
        Flux<String> tick = Flux.interval(Duration.ofMillis(10))
                .take(6)
                //.doOnNext(x->log.info("next ... {}", x))
                .onBackpressureBuffer(6)
                .flatMap(i-> Mono.fromCallable(()->{
                            System.out.println("simulate IO " + Thread.currentThread() + "  " + i);
                            sleep(1500L); // simulate IO delay, very slow
                            return String.format("String %d", i);
                        }).subscribeOn(Schedulers.boundedElastic())
                        , 3);

        Disposable disposable = tick.subscribe(x ->System.out.println("Subscribe thread: " + Thread.currentThread() + "  --> " + x),
                System.out::println,
                ()-> System.out.println("Done"));

        while(!disposable.isDisposed()){
            sleep(800);
            System.out.println("..wait..");
        }
        System.out.println("DONE AND DONE");

    }

    public static void sleep(long timeMilli) {
        try {
            Thread.sleep(timeMilli);
        } catch (InterruptedException e) {
            System.out.println("Exiting");
        }
    }
}
