package com.example.flux.model;

import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
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
