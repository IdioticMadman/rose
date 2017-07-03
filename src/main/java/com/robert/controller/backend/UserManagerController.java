package com.robert.controller.backend;

import com.robert.common.Constants;
import com.robert.common.ServerResponse;
import com.robert.pojo.User;
import com.robert.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by robert on 2017/6/23.
 */

@Controller
@RequestMapping("/manage/user")
public class UserManagerController {


    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccessful()) {
            User user = response.getData();
            if (user.getRole() == Constants.Role.ROLE_ADMIN) {
                //说明登录的是管理员
                session.setAttribute(Constants.CURRENT_USER, user);
                return response;
            } else {
                return ServerResponse.createByErrorMessage("不是管理员,无法登录");
            }
        }
        return response;
    }
}
