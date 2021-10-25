package com.example.flux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * demo
 * 1) open resource
 * 2) process resource (throw exception when error encounter)
 * 3) close resource
 * retry (x times) on error
 *
 */
@Log4j2
public class FluxErrorRetry2Tests {

    static public void closeStream(Closeable s){
        try{
            log.info("close io-stream {}", s);
            if(s!=null)s.close();
        }catch(IOException e){
            log.error(e);
        }
    }

    static int processStream(InputStream ins) {
        byte[] buf = new byte[3];
        int total = 0;

        log.info("process io-stream {}", ins);

        try {
            while (ins.available()>0){
                final int len = ins.read(buf);
                total += len;
                log.info("read ----> {}", total);
                if(total>=10){
                    throw new RuntimeException("fake error");
                }
            }
        } catch (IOException e) {
            log.error(e);
        } finally {
            closeStream(ins);
        }
        
        return total;
    }
    
    public static InputStream openStream(String data) {
        log.info("open io-stream {}", data);
        return  new ByteArrayInputStream(data.getBytes());
    }

    /**
     * test retry with error happening
     * when string length >= 10, will trigger runtime exception
     */
    @Test
    void testMonoErrorRetry(){
        Mono.just("hello world")
                .map(x-> openStream(x))
                .map(x->processStream(x))
                .retry(2)
                .doOnError(e->log.info("something wrong with {}", e.toString()))
                .doOnSuccess(e->log.info("success with {}", e))
                .onErrorResume((e)->Mono.just(0))  // to disable the last exception
                .doFinally(e->{
                    log.info("done with {}", e);
                })
                .block();
    }

    /**
     * retry not happening
     */
    @Test
    void testMonoNoRetry(){
        Mono.just("hello")
                .map(x-> openStream(x))
                .map(x->processStream(x))
                .retry(2)
                .doOnError(e->log.info("something wrong with {}", e.toString()))
                .doOnSuccess(e->log.info("success with {}", e))
                .onErrorResume((e)->Mono.just(0))
                .doFinally(e->{
                    log.info("done with {}", e);
                })
                .block();
    }

}
