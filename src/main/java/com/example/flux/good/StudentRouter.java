package com.example.flux.good;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;



@Configuration
public class StudentRouter {

    @Bean
    public RouterFunction<ServerResponse>  composedRoutes(StudentHandler studentProcess) {
        return RouterFunctions
                .route(GET("/students/{id:[0-9]+}"), studentProcess::getStudent)
                .andRoute(GET("/students"), studentProcess::listStudents)
                .andRoute(GET("/students/admin"), studentProcess::admin);
    }
}
