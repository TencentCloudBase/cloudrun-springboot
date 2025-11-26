package com.tencent.cloudrun.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Value;

@Data
public class User {
    @JsonProperty(value = "id")
    private int id;

    @JsonProperty(value = "name")
    private String name;
}
