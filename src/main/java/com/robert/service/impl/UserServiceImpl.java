package com.robert.service.impl;

import com.robert.common.Constants;
import com.robert.common.ServerResponse;
import com.robert.common.TokenCache;
import com.robert.dao.UserMapper;
import com.robert.pojo.User;
import com.robert.service.IUserService;
import com.robert.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by robert on 2017/6/22.
 */

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     * 1.判断用户是否存在
     * 2.判断密码是否正确
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUserName(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.login(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createSuccess("登陆成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> serverResponse = checkValid(user.getUsername(), Constants.USERNAME);
        if (!serverResponse.isSuccessful()) {
            return serverResponse;
        }
        serverResponse = checkValid(user.getEmail(), Constants.EMAIL);
        if (!serverResponse.isSuccessful()) {
            return serverResponse;
        }
        user.setRole(Constants.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        if (userMapper.insert(user) > 0) {
            return ServerResponse.createSuccessMessage("注册成功");
        } else {
            return ServerResponse.createByErrorMessage("注册失败");
        }
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isBlank(type)) {
            return ServerResponse.createByErrorMessage("参数错误");
        } else {
            type = type.toUpperCase();
            if (StringUtils.equals(Constants.USERNAME, type)) {
                int resultCount = userMapper.checkUserName(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }

            if (StringUtils.equals(Constants.EMAIL, type)) {
                int resultCount = userMapper.checkUserEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已被注册");
                }
            }
        }
        return ServerResponse.createSuccess("校验成功");
    }

    @Override
    public ServerResponse<String> forgetGetQuestion(String username) {
        //这个用户名没有存入个过数据库，则表示没有这个用户
        if (checkValid(username, Constants.USERNAME).isSuccessful()) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String question = userMapper.selectProblemByName(username);
        if (StringUtils.isBlank(question)) {
            return ServerResponse.createByErrorMessage("暂未设置问题");
        }
        return ServerResponse.createSuccess(question);
    }

    @Override
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.put(TokenCache.prefix + username, forgetToken);
            return ServerResponse.createSuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题答案不正确");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，token需传递");
        }
        if (checkValid(username, Constants.USERNAME).isSuccessful()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String cacheToken = TokenCache.get(TokenCache.prefix + username);
        if (StringUtils.isBlank(cacheToken)) {
            return ServerResponse.createByErrorMessage("Token过时");
        }

        if (StringUtils.equals(cacheToken, forgetToken)) {
            String password = MD5Util.MD5EncodeUtf8(newPassword);
            int count = userMapper.updateUserPasswordByUsername(username, password);
            if (count > 0) {
                return ServerResponse.createSuccessMessage("重置密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("Token错误，请重新回答问题获取");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user) {
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createSuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }



    @Override
    public ServerResponse<User> updateInfo(User user) {
        int resultCount = userMapper.checkUserEmailById(user.getId(), user.getEmail());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("该邮箱已经存在，请尝试重新填写");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int count = userMapper.updateByPrimaryKeySelective(user);
        if (count > 0) {
            return ServerResponse.createSuccess("更新个人信息成功", user);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createSuccess(user);
    }

    //backend

    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
    @Override
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Constants.Role.ROLE_ADMIN){
            return ServerResponse.createSuccess();
        }
        return ServerResponse.createByError();
    }


}
