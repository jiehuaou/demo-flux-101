package com.example.flux.controlle;

import com.example.flux.model.Hello;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@RestController
public class ReactiveController {

    @Autowired
    WebClient.Builder builder;

    /**
    *   web service of Mono<Hello>
     */
    @RequestMapping(value = "/say/{name}", method = RequestMethod.GET)
    public ResponseEntity<Mono<Hello>> say(@PathVariable String name){
        log.info("GET:/say/{} -----> invoked", name);
        Mono<Hello> mono = Mono.just(new Hello("hello", name)).log();
        return ResponseEntity.ok(mono);
    }

    @RequestMapping(value = "/say/{name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<Hello>> sayWithBody(@PathVariable String name, @RequestBody String body){
        log.info("POST:/say/{}  {} -----> invoked", name, body);
        Mono<Hello> mono = Mono.just(new Hello("hello " + body, name)).log();
        return ResponseEntity.ok(mono);
    }

    /**
     *   web service of Flux<Hello>
     */
    @RequestMapping(value = "/hello",  produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Hello> helloAll(){
        Flux<Hello> ret = Flux.just(new Hello("hello", "a1"),
                new Hello("hello", "a2"),
                new Hello("hello", "a3")
        ).delayElements(Duration.ofMillis(100)).log();

        return ret;
    }

    /**
     * call /say/www by WebClient
     */
    @RequestMapping("/say-www")
    public ResponseEntity<Mono<Hello>> sayWWW(){
        Mono<Hello> hello = builder.baseUrl("http://localhost:8080").build()
                .get()
                .uri("/say/www")
                .headers(headers -> headers.setBasicAuth("admin", "123"))
                .retrieve()
                .bodyToMono(Hello.class)
                .log();

        log.info("/say-www  -----> invoked");
        return ResponseEntity.ok(hello);
    }

    /**
     * call /say/www by WebClient
     */
    @RequestMapping("/say-www2")
    public ResponseEntity<Mono<Hello>> sayWWW2(){
        String basicAuthHeader = "Basic " + Base64Utils.encodeToString(("admin:123" ).getBytes());
        Mono<Hello> hello = builder.baseUrl("http://localhost:8080").build()
                .post()
                .uri("/say/www")
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                .body(Mono.just("abc"), String.class)
                .retrieve()
                .bodyToMono(Hello.class);

        log.info("/say-www 2 -----> invoked");
        return ResponseEntity.ok(hello);
    }

}
