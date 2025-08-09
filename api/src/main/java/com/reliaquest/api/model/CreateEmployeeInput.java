package com.reliaquest.api.model;

import lombok.Data;

@Data
public class CreateEmployeeInput {
    private String name;
    private Integer salary;
    private Integer age;
    private String title;
}
