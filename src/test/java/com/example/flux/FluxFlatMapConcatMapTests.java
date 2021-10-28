package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Flux: FlatMap vs ConcatMap
 */

@Log4j2
public class FluxFlatMapConcatMapTests {

    /**
     * 即同时订阅多个流，然后按时间先后顺序 merge(消费) 流中的元素
     * flatMap - run in multiple thread, element output may be in different order
     * ["a1","a2","a3","a4","a5","a6"] -> ["a3","a1","a2","a6","a5","a4"]
     */
    @Test
    void testFlatMap()  {

        //CountDownLatch countDown = new CountDownLatch(1);

        Flux<String> data = Flux.just("1","2","3","4","5","6");

        data.flatMap(x->getEmployeeName(x), 3)  // with concurrency 3
                .doOnNext(x->log.info(x))
                .doFinally((x)->log.info("----- done -----"))
                .blockLast();

    }

    /**
     * 即先订阅流1消费流1，待流1全部消费完成后, 再订阅流2消费流2
     * concatMap - run in multiple thread, element output must be in original order
     * [1,2,3,4,5,6] -> [1,2,3,4,5,6]
     */
    @Test
    void testConcatMap(){
        Flux<String> data = Flux.just("1","2","3","4","5","6");

        data.concatMap(x->getEmployeeName(x))
                .doOnNext(x->log.info(x))
                .doFinally((x)->log.info("----- done -----"))
                .blockLast();
        log.info("----- test end -----");
    }

    /**
     * 即同时订阅多个流，然后先消费流1，积累流2，直到流1消费完成后再消费流2
     * flatMapSequential - run in multiple thread, element output must be in original order
     * [1,2,3,4,5,6] -> [1,2,3,4,5,6]
     */
    @Test
    void testFlatMapSeq(){
        Flux<String> data = Flux.just("1","2","3","4","5","6");

        data.flatMapSequential(x->getEmployeeName(x), 3)
                .doOnNext(x->log.info(x))
                .doFinally((x)->log.info("----- done -----"))
                .blockLast();
        log.info("----- test end -----");
    }

    static HashMap<String, String> employees = new HashMap<>();
    static {
        employees.put("1", "1.sasa");
        employees.put("2", "2.billy");
        employees.put("3", "3.cindy");
        employees.put("4", "4.davide");
        employees.put("5", "5.ella");
        employees.put("6", "6.flee");
        employees.put("7", "7.galosh");
        employees.put("8", "8.hank");
    }
    //mock reactive data search service
    private Mono<String> getEmployeeName(String id) {

        String name = employees.get(id);
//        try {
//            Thread.sleep(getRandom());
////            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException("Thread sleep Error!!!");
//        }
        if (name==null) {
            throw new RuntimeException("Not Found!!!");
        }
        int random = getRandom();
        String result = String.format("%s [delay %d ms]",  name, random);
        return Mono.just(result).delayElement(Duration.ofMillis(random));
    }

    final static Random rx = new Random();
    int getRandom(){
        return rx.nextInt(100) * 5 + 300;
    }

    @Test
    void testRandom(){

        for (int i = 0; i < 10; i++) {
            int r = getRandom();
            log.info("random --> {}", r);
        }
    }

}
