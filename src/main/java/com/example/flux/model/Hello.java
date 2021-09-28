package com.example.flux.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hello {
    private String message ;
    private String name;

//    public Hello(String message, String name) {
//        this.message = message;
//        this.name = name;
//    }
}
