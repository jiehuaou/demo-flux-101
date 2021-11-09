package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Log4j2
public class FluxScheduleTests {

    /**
     * first subscribeOn(schedulerA) normally affect all operation
     * subsequent subscribeOn does not take effect
     * subsequent publishOn will affect following operator
     *
     */
    @Test
    public void publishSubscribeExample() {
        Scheduler schedulerA = Schedulers.newParallel("Scheduler A");
        Scheduler schedulerB = Schedulers.newParallel("Scheduler B");
        Scheduler schedulerC = Schedulers.newParallel("Scheduler C");
        Flux.just("x")
                .map(i -> {
                    System.out.println("First map: " + Thread.currentThread().getName());
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler A"));
                    return i; // Scheduler A
                })
                .subscribeOn(schedulerA) // first subscribeOn() affect all normally
                .map(i -> {
                    System.out.println("Second map: " + Thread.currentThread().getName());
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler A"));
                    return i;  // Scheduler A
                })
                .publishOn(schedulerB) // this publishOn change the schedule of previous subscribeOn()
                .map(i -> {
                    System.out.println("Third map: " + Thread.currentThread().getName());
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler B"));
                    return i; // Scheduler B
                })
                .subscribeOn(schedulerC) // subsequent subscribeOn does not take effect
                .map(i -> {
                    System.out.println("Fourth map: " + Thread.currentThread().getName());
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler B"));
                    return i; // Scheduler B
                })
                .publishOn(schedulerA)
                .map(i -> {
                    System.out.println("Fifth map: " + Thread.currentThread().getName());
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler A"));
                    return i; // Scheduler A
                })
                .blockLast();
    }

    // publishOn is applied in the middle of a chain. It affects subsequent operators after publishOn -
    // they will be executed on a thread picked from publishOn's scheduler.
    @Test
    void testPubOn(){
        Scheduler schedulerA = Schedulers.newParallel("Scheduler A");
        Flux.range(1, 2)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    assertThat(Thread.currentThread().getName(), containsString("main"));
                    return i;
                })
                .publishOn(schedulerA)
                .map(i -> {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler A"));
                    return i;
                })
                .blockLast();
    }

    //  If you place a subscribeOn in a chain, it affects the source emission in the entire chain.
    @Test
    void testSubOn(){
        Scheduler schedulerA = Schedulers.newParallel("Scheduler A");
        Flux.range(1, 2)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler A"));
                    return i;
                })
                .subscribeOn(schedulerA)
                .map(i -> {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler A"));
                    return i;
                })
                .blockLast();
    }
    // If you define multiple publishOn, It affects subsequent operators after publishOn
    @Test
    void testMultiPubOn(){
        Scheduler schedulerA = Schedulers.newParallel("Scheduler A");
        Scheduler schedulerB = Schedulers.newParallel("Scheduler B");

        Flux.range(1, 2)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    assertThat(Thread.currentThread().getName(), containsString("main"));
                    return i;
                })
                .publishOn(schedulerA) // affect subsequent operators
                .map(i -> {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler A"));
                    return i;
                })
                .publishOn(schedulerB) // affect subsequent operators
                .map(i -> {
                    System.out.println(String.format("Third map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler B"));
                    return i;
                })
                .blockLast();
    }

    //  If you define multiple subscribeOn operators in a chain, it will use the first one.
    @Test
    void testMultiSubOn(){
        Scheduler schedulerA = Schedulers.newParallel("Scheduler A");
        Scheduler schedulerB = Schedulers.newParallel("Scheduler B");

        Flux.range(1, 2)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler A"));
                    return i;
                })
                .subscribeOn(schedulerA) // first subscribeOn affects all operators
                .map(i -> {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler A"));
                    return i;
                })
                .subscribeOn(schedulerB) // second subscribeOn(scheduler-B) is ignored
                .map(i -> {
                    System.out.println(String.format("Third map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    assertThat(Thread.currentThread().getName(), containsString("Scheduler A"));
                    return i;
                })
                .blockLast();
    }

    @Test
    void testAfterPubOn(){
        Scheduler schedulerA = Schedulers.newParallel("Scheduler A");
        Scheduler schedulerB = Schedulers.newParallel("Scheduler B");

        Flux.just(1)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .publishOn(schedulerA)
                .map(i -> {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .subscribeOn(schedulerB)
                .map(i -> {
                    System.out.println(String.format("Third map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .blockLast();
    }

    @Test
    void testNestedChain(){
        Scheduler schedulerA = Schedulers.newParallel("Scheduler A");
        Scheduler schedulerB = Schedulers.newParallel("Scheduler B");

        Flux.just(1)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .subscribeOn(schedulerA)
                .map(i -> {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));

                    return Flux
                            .just( 2)
                            .map(j -> {
                                System.out.println(String.format("First map - (%s.%s), Thread: %s", i, j, Thread.currentThread().getName()));
                                return j;
                            })
                            .subscribeOn(schedulerB)
                            .map(j -> {
                                System.out.println(String.format("Second map - (%s.%s), Thread: %s", i, j, Thread.currentThread().getName()));
                                return "value " + j;
                            }).subscribe();
                })
                .blockLast();
    }

}
