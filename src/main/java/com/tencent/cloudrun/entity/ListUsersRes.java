package com.tencent.cloudrun.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.cloudrun.model.User;
import lombok.Data;

import java.util.List;

@Data
public class ListUsersRes {
    @JsonProperty(value = "total")
    private int total;

    @JsonProperty(value = "items")
    private List<User> items;
}
