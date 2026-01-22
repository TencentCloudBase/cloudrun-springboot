package com.tencent.cloudrun.controller;

import com.tencent.cloudrun.dto.ApiResponse;
import com.tencent.cloudrun.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final List<User> users = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public UserController() {
        // 初始化测试数据
        users.add(new User(counter.incrementAndGet(), "张三", "zhangsan@example.com"));
        users.add(new User(counter.incrementAndGet(), "李四", "lisi@example.com"));
        users.add(new User(counter.incrementAndGet(), "王五", "wangwu@example.com"));
    }

    @GetMapping
    public ApiResponse<List<User>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        int startIndex = (page - 1) * limit;
        int endIndex = Math.min(startIndex + limit, users.size());
        
        if (startIndex >= users.size()) {
            return ApiResponse.success(new ArrayList<>());
        }
        
        List<User> paginatedUsers = users.subList(startIndex, endIndex);
        return ApiResponse.success(paginatedUsers);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        
        return ApiResponse.success(user);
    }

    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        if (user.getName() == null || user.getEmail() == null) {
            return ApiResponse.error("姓名和邮箱不能为空");
        }
        
        user.setId(counter.incrementAndGet());
        users.add(user);
        
        return ApiResponse.success(user);
    }
}
