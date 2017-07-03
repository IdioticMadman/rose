package com.robert.common;

/**
 * Created by robert on 2017/6/22.
 */
public class Constants {

    public static final String CURRENT_USER = "CURRENT_USER";

    public static final String USERNAME = "USERNAME";

    public static final String EMAIL = "EMAIL";

    public interface Role {
        int ROLE_CUSTOMER = 0;
        int ROLE_ADMIN = 1;
    }
}
