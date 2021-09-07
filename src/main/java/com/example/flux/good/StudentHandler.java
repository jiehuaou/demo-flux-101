package com.example.flux.good;

import com.fasterxml.jackson.databind.BeanProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class StudentHandler {

    Mono<Student> getById(Long id){
        if(id!=100){
            return Mono.just(new Student(id, "a1", "b1"));
        }
        return Mono.empty();
    }

    public Mono<ServerResponse> getStudent(ServerRequest serverRequest) {
        Long id =  Long.parseLong(serverRequest.pathVariable("id"));

        return getById(id)
                .flatMap(student -> ServerResponse.ok().body(fromValue(student)))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> listStudents(ServerRequest serverRequest) {
        String name = serverRequest.queryParam("name").orElse("unknown");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Flux.just(
                        new Student(100L, name, "last-name-1"),
                        new Student(101L, name, "last-name-2"),
                        new Student(102L, name, "last-name-3")
                ), Student.class);
    }

    public Mono<ServerResponse> admin(ServerRequest serverRequest) {
        //String name = serverRequest.queryParam("name").orElse("unknown");
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("hello admin"), String.class);
    }
}
