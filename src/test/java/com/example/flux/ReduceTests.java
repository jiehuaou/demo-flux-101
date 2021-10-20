package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
public class ReduceTests {
    @Test
    void testReduce1(){
        Flux<Integer> data = Flux.just(1,3,5,7,9);

        data
            .reduce(0, (x, y)->{
                log.info("{} + {} = {}", x, y, x+y);
                return x+y;
            })
            .doOnSuccess(System.out::println)
            //    .then()
            .subscribe();
    }

    @Test
    void testReduce2(){
        Flux<Integer> data = Flux.just(1,3,5,7,9);

        data
                .reduceWith(()->0, (temp, y)->{
                    log.info("concat {} + {} = {}", temp , y, temp + y);
                    return temp + y;
                })
                //.doOnNext(System.out::println)
                .then()
                .subscribe();
    }
}
