package com.example.flux;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * class Response1{
 *    String a1;
 *    String a2;
 * }
 *
 * class Response2{
 *    String b1;
 * }
 *
 * class Response3{
 *    String c1;
 * }
 *
 * class Response4{
 *    String d1;
 * }
 *
 * Flux<Response1> service1();
 * Flux<Response2> service2(String a1); //based on field a1 of Response1(service 1)
 * Flux<Response3> service3(String b1); //based on field b1 of Response2(service 2)
 * Mono<Response4> service4(String a2); //based on field a2 of Response1(service 1)
 *
 */

@Log4j2
public class FluxCombiningTests {
    @Data
    @AllArgsConstructor
    @ToString
    static class Response1{
        String a1;
    }
    @Data
    @AllArgsConstructor
    @ToString
    static class Response2{
        String b1;
    }
    @Data
    @AllArgsConstructor
    @ToString
    static class Response3{
        String c1;
    }
    @Data
    @AllArgsConstructor
    @ToString
    static class Response4{
        String d1;
    }

    @Data
    @ToString
    @Builder
    static class AggResponse{
        Response1 response1;
        Response2 response2;
        Response3 response3;
        Response4 response4;
    }

    static class Pair<T1, T2>{
        T1 data1;
        T2 data2;
        public Pair(T1 t1, T2 t2){
            data1 = t1;
            data2 = t2;
        }
    }

    static Flux<Response1> service1(){
        return Flux.just(new Response1("a1")).delayElements(Duration.ofMillis(4));
    }
    static Flux<Response2> service2(String a1){
        return Flux.just(new Response2("b1" + a1), new Response2("b2" + a1)).delayElements(Duration.ofMillis(3));
    }
    static Flux<Response3> service3(String b1){
        return Flux.just(new Response3("c1" + b1)).delayElements(Duration.ofMillis(5));
    }
    static Flux<Response4> service4(String a2){
        return Flux.just(new Response4("c1" + a2)).delayElements(Duration.ofMillis(8));
    }

    static Flux<Pair<Response2, Response3>> service2Service3(String a1){
        return service2(a1)
                .flatMap(x2->service3(x2.b1)
                        .map(x3->new Pair<>(x2, x3))
                )
        ;
    }

    @Test
    void test1(){
        service1().doOnNext(e->log.info(e)).blockLast();
    }

    @Test
    void testMerge(){
        service1().flatMap(response1 -> Flux.merge(
                service2(response1.a1),
                service4(response1.a1)
        ))
                .doOnNext(e->log.info(e))
                .blockLast();
    }
    @Test
    void testConcat(){
        service1().flatMap(response1 -> Flux.concat(
                service2(response1.a1),
                service4(response1.a1)
        ))
                .doOnNext(e->log.info(e))
                .blockLast();
    }

    /**
     * 1 service1() -----> * service2() --> 1 service3()
     *                |--> 1 service4()
     */
    @Test
    void testCombineLatest(){
        service1().flatMap(response1 -> Flux.combineLatest(
                service2Service3(response1.a1),
                service4(response1.a1),
                (pair, p4)->{
                    //log.info(agg);
                    //log.info(p4);
                    AggResponse agg = AggResponse.builder()
                            .response1(response1)
                            .response4(p4)
                            .response2(pair.data1)
                            .response3(pair.data2)
                            .build();
                    return agg;
                }
        ))
                .doOnNext(e->log.info(e))
                .blockLast();
    }
}
