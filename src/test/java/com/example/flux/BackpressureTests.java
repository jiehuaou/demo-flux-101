package com.example.flux;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
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
}
