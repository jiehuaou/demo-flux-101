package com.example.flux.controlle.status;

import com.example.flux.model.ErrorBody;
import com.example.flux.model.Hello;
import com.example.flux.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class SubController {

    @RequestMapping(value = "/sub/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<Student>>  getStudent(@PathVariable String id) {
        if ("err".equalsIgnoreCase(id)) {
            throw new  ErrorException();
        }

        if (isNotNumber(id)) {
           throw new BedRequestException();
        }

        return ResponseEntity.ok(Mono.just(Student.builder()
                .studentId(Long.valueOf(id))
                .firstName("hello")
                .lastName("world")
                .build()));
    }

    private boolean isNotNumber(String id) {
        try {
            Long.valueOf(id);
            return false;
        } catch (Exception ex) {
            return true;
        }

    }
}
