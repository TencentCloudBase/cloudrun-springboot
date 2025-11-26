package com.tencent.cloudrun.controller;

import com.tencent.cloudrun.entity.ListUsersRes;
import com.tencent.cloudrun.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping(value = "/api/users")
    public ListUsersRes ListUser() {
        return userService.listUsers();
    }
}
