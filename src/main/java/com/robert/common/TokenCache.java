package com.robert.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by robert on 2017/6/23.
 */
public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String prefix = "token_";

    private static LoadingCache<String, String> localCache =
            CacheBuilder.newBuilder()
                    .initialCapacity(1000)
                    .maximumSize(10000)
                    .expireAfterAccess(12, TimeUnit.HOURS)
                    .build(new CacheLoader<String, String>() {
                        //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
                        @Override
                        public String load(String key) throws Exception {
                            return "null";
                        }
                    });


    public static void put(String key, String value) {
        localCache.put(key, value);
    }

    public static String get(String key) {
        String value = null;
        try {
            value = localCache.get(key);
            if (StringUtils.equals("null", value)) {
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            e.printStackTrace();
            logger.error("get cache error", e);
        }
        return null;
    }
}
