package com.example.flux.controlle.status;

import com.example.flux.model.ErrorBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(BedRequestException.class)
    public ResponseEntity handleException1(BedRequestException e) {
        // log exception
        return ResponseEntity.internalServerError()
                .body(Mono.just(ErrorBody.builder().code("wrong id").build()));
    }

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity handleException2(ErrorException e) {
        // log exception
        return ResponseEntity.internalServerError()
                .body(Mono.just(ErrorBody.builder().code("something wrong").build()));
    }
}
