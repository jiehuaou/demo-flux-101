package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.SynchronousSink;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@Log4j2
public class FluxCreateGenerateTests {

    /**
     * Flux.create() :
     *
     * 1. Consumer can emit N elements immediately,
     * 2. Publisher is not aware of downstream state,
     * 3. So we need to provide Overflow strategy as an additional parameter,
     * we could even keep on emitting elements using multiple threads if required
     */
    @Test
    void testFluxCreate1(){
        Flux<Integer> integerFlux = Flux.create((FluxSink<Integer> fluxSink) -> {
            IntStream.range(0, 500)
                    .peek(i -> log.info("going to emit - " + i))
                    .forEach(fluxSink::next);
            fluxSink.complete();
        });

        log.info("flux created ...");
        //First observer. takes 1 ms to process each element
        // integerFlux.delayElements(Duration.ofMillis(10)).subscribe(i -> System.out.println("First :: " + i));

        //Second observer. takes 2 ms to process each element
        Disposable ref = integerFlux
                .delayElements(Duration.ofMillis(10))
                .doOnComplete(()->log.info(" ==== end ==== "))
                .subscribe(i -> log.info("Second:: " + i));

        Task.waiting(ref);

        log.info("flux subscribe end ...");

    }

    public static class FluxSinkImpl implements Consumer<FluxSink<Integer>> {

        private FluxSink<Integer> fluxSink;

        @Override
        public void accept(FluxSink<Integer> integerFluxSink) {
            this.fluxSink = integerFluxSink;
        }

        public void publishEvent(int event){
            System.out.println("going to emit - " + event);
            this.fluxSink.next(event);
        }

    }

    @Test
    void testFluxCreate2Consumer(){
        //create an instance of FluxSink implementation
        FluxSinkImpl fluxSinkConsumer = new FluxSinkImpl();

        //create method can accept this instance
        Flux<Integer> integerFlux = Flux.create(fluxSinkConsumer).delayElements(Duration.ofMillis(1)).share();
        //integerFlux.delayElements(Duration.ofMillis(1)).subscribe(i -> System.out.println("First :: " + i));
        Disposable ref = integerFlux.delayElements(Duration.ofMillis(200)).subscribe(i -> System.out.println("Second:: " + i));

        //We emit elements here
        IntStream.range(0, 5000)
                .forEach(ev->fluxSinkConsumer.publishEvent(ev));

        Task.waiting(ref);

    }



    /**
     * Flux.generate() works gentle and
     *
     * 1. Consumer can emit only one element,
     * 2. Consumer is invoked again and again based on the downstream demand,
     * 3. Publisher produces elements based on the downstream demand,
     */
    @Test
    void testFluxGenerate1(){
        AtomicInteger counter = new AtomicInteger(100);

        //Flux generate sequence
        Flux<Integer> integerFlux = Flux.generate((SynchronousSink<Integer> synchronousSink) -> {
            log.info("Flux generate ** " + counter.get());
            synchronousSink.next(counter.getAndIncrement());
            if(counter.get()>=200){
                synchronousSink.complete();
            }
        });

        //observer
        Disposable ref =  integerFlux
                .delayElements(Duration.ofMillis(500))
                .doOnComplete(()->log.info(" ==== end ==== "))
                .subscribe(i -> log.info("First consumed :: " + i));

        Task.waiting(ref);
    }

    /**
     * Flux.generate() with initial state
     */
    @Test
    void testFluxGenerate2State(){

        //Flux which accepts initial state and bi-function as arg
        Flux<Integer> intFlux = Flux.generate(
                ()->100,
                (state, sink)->{
                    Integer value = state.intValue();
                    log.info("Flux generate : " + value);
                    sink.next(value);
                    // sink.next(value);  // not allowed for IllegalStateException: More than one call to onNext
                    if(value>=200){
                        sink.complete();
                    }
                    return value+1;
                });

        //Observer
        Disposable ref = intFlux
                .delayElements(Duration.ofMillis(50))
                .doOnComplete(()->log.info(" ==== end ==== "))
                .subscribe(i -> log.info("Consumed ::" + i));

        Task.waiting(ref);

    }
}
