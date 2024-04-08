package com.ink.consumer;

import com.ink.common.model.User;
import com.ink.common.service.UserService;
import com.ink.rpc.proxy.ServiceProxyFactory;

public class EasyConsumerExample {
    public static void main(String[] args) {
        UserService service = ServiceProxyFactory.getProxy(UserService.class);

        User user = new User();
        user.setName("设置的用户名");

        User newUser = service.getUser(user);

        if(newUser == null){
            System.out.println("user 为空");
        }else{
            System.out.println(user.getName());
        }
    }
}
