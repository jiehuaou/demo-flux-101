package com.example.flux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
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
        CountDownLatch latch = new CountDownLatch(1);


            Flux.range(1, 15)
                .parallel(3)
                .runOn(Schedulers.elastic())
                .doOnNext(i -> {
                    {
                        System.out.println(String.format("Executing %s on thread %s", i, Thread.currentThread().getName()));
                        doSomething();
                        System.out.println(String.format("Finish executing %s", i));
                    }
                })
                .sequential()
                .doOnNext(System.out::println)
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
                        System.out.println(String.format("Executing %s on thread %s", i, Thread.currentThread().getName()));
                        doSomething();
                        System.out.println(String.format("Finish executing %s", i));
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
