package com.example.flux.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class ErrorBody {
    private String code;
}
