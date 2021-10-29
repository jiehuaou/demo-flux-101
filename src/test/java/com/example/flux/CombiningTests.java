package com.example.flux;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static com.example.flux.Task.singleTaskSchedule;
import static com.example.flux.Task.waiting;

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
public class CombiningTests {
    @Data
    @AllArgsConstructor
    @ToString
    static class Response1{
        String a1;
        String a2;
    }

    @Data
    @AllArgsConstructor
    @ToString
    static class Response2{
        String b1;
        public Response2(){
        }
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
    @ToString(callSuper=true)
    static class AggResponse2 extends Response2{
        List<Response3> response3s;
        public AggResponse2(String b1, List<Response3> response3s) {
            super(b1);
            this.response3s = response3s;
        }
    }

    @Data
    @ToString
    @Builder
    static class FinalResponse {
        final String a1;
        final String a2;
        final String d1;
        final List<AggResponse2> response2s;
    }

    static Flux<Response1> service1(){
        //
        return Flux
                .just(new Response1("a1", "a2"))
                .delayElements(Duration.ofMillis(4));
    }
    static Flux<Response2> service2(String a1){
        //
        return Flux
                .just(new Response2("b1-" + a1), new Response2("b2-" + a1))
                .delayElements(Duration.ofMillis(3));
    }
    static Flux<Response3> service3(String b1){
        //
        return Flux
                .just(new Response3("c1-" + b1), new Response3("c2-" + b1))
                .delayElements(Duration.ofMillis(5));
    }
    static Mono<Response4> service4(String a2){
        //
        return Mono
                .just(new Response4("d1-" + a2))
                .delayElement(Duration.ofMillis(8));
    }

    static Flux<AggResponse2> service2Service3(String a1){
        return service2(a1)
                .flatMap(
                        x2->service3(x2.b1)
                                .collectList()
                                .map(x3->new AggResponse2(x2.b1, x3))
                );
    }

    @Test
    void testService2Service3(){

        AggResponse2 agg = new AggResponse2("xx", null);
        agg.setB1("bb");
        //
        service2Service3("a1")
                .doOnNext(e->log.info(e))
                .blockLast();
    }

    @Test
    void test1(){
        //
        service1()
                .doOnNext(e->log.info(e))
                .blockLast();

        service2("a1")
                .doOnNext(e->log.info(e))
                .blockLast();
    }

    /**
     * zip ( (1,2,3), (a, b) ) --> (1, a) (2, b)
     */
    @Test
    void testZip(){
        Flux
                .zip(Flux.just(1,2,3), Flux.just("a", "b"))
                .doOnNext(x->{
                    log.info("{} AND {}", x.get(0), x.get(1));
                }).blockLast();
    }

    /**
     * merge ((1,2,3), (a,b,c)) --> async (1,a,2,3,b,c)
     */
    @Test
    void testMerge(){
        service1()
                .flatMap(response1 -> Flux.merge(
                        Flux.just(1,2,3), Flux.just("a", "b")
//                        service2(response1.a1), service4(response1.a1)
                    )
                )
                .doOnNext(e->log.info(e))
                .blockLast();
    }

    /**
     * concat ((1,2,3), (a,b,c)) --> (1,2,3,a,b,c)
     */
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
     * combineLatest( (1,2,3), (4,5,6) ) --> (1,4), (2,4), (2,5), (3,5), (3,6)
     */
    @Test
    void testCombineLatest(){
        Flux.combineLatest(
                Flux.just(1,2,3).delayElements(Duration.ofMillis(500)),
                Flux.just(4,5,6).delayElements(Duration.ofMillis(550)),
                (p1, p2)-> p1*10 + p2
        )
                .doOnNext(e->log.info(e))
                //.map(x->1)
                .reduce(0, (a,b)->a+b)
                .doOnNext(e->log.info("total -> {}", e))
                .block();
    }

    /**
     * combine ( Mono A , Mono B ) --> Mono C
     */
    @Test
    void testCombine2Mono(){
        Disposable disposable = Flux.combineLatest(
                singleTaskSchedule("A"),
                singleTaskSchedule("B"),
                (a,b)-> a+"+"+b
        )
                .next()
                .subscribe(s->log.info(s));
        waiting(disposable);
    }

    /**
     * service1() 1 -----> * service2() 1 --> * service3()
     *                |--> 1 service4()
     */
    @Test
    void testComplexCombineLatest(){
        ObjectMapper objectMapper = new ObjectMapper();
        service1().flatMap(response1 -> Flux.combineLatest(
                service2Service3(response1.a1).collectList(), // call service2 which call service3
                service4(response1.a2),                       // call service4
                (aggResponse2, response4)->{
                    //log.info(agg);
                    //log.info(response4);
                    FinalResponse agg = FinalResponse.builder()
                            .a1(response1.a1)
                            .a2(response1.a2)
                            .d1(response4.d1)
                            .response2s(aggResponse2)
                            .build();
                    return agg;
                })
            ).doOnNext(e->{
                    try {
                        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(e);
                        System.out.println("JSON = " + json);
                    } catch (Exception ex) {
                        //e.printStackTrace();
                    }
            }).blockLast();
    }
}
