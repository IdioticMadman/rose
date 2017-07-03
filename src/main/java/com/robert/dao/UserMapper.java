package com.robert.dao;

import com.robert.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User login(@Param("username") String username, @Param("password") String password);

    int checkUserName(String username);

    int checkUserEmail(String email);

    String selectProblemByName(String username);

    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    int updateUserPasswordByUsername(@Param("username") String username, @Param("password") String password);

    int checkUserEmailById(@Param("id") Integer id, @Param("email") String email);

    int checkPassword(@Param("password") String password,@Param("id") Integer id);
}