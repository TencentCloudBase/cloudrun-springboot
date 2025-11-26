package com.tencent.cloudrun.service.impl;

import com.tencent.cloudrun.entity.ListUsersRes;
import com.tencent.cloudrun.model.User;
import com.tencent.cloudrun.service.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    public ListUsersRes listUsers() {
        List<User> userList = new ArrayList<>();
        // 添加第一个用户
        User user_0 = new User();
        user_0.setId(0);
        user_0.setName("zhangsan");
        userList.add(user_0);

        // 添加第二个用户
        User user_1 = new User();
        user_1.setId(1);
        user_1.setName("lisi");
        userList.add(user_1);

        ListUsersRes listUsersRes = new ListUsersRes();
        listUsersRes.setTotal(2);
        listUsersRes.setItems(userList);
        return listUsersRes;
    }
}
