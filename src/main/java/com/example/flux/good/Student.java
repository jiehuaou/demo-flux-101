package com.example.flux.good;

import lombok.Data;

@Data
public class Student {
    private Long studentId;
    private String firstName;
    private String lastName;

    public Student(Long studentId, String firstName, String lastName) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
