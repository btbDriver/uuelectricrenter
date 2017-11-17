package com.youyou.uuelectric.renter;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * Created by liuchao on 2015/9/2.
 * SP工具类单元测试
 */
public class SharedPreferenceTest extends ApplicationTestCase<Application> {
    public SharedPreferenceTest() {
        super(Application.class);
    }

    public void test1() {
        /*UserInfo userInfo = new UserInfo();
        userInfo.setNeedFlushSecs(123);
        userInfo.setUnvalidSecs(234);
        byte[] byt = "liuchao".getBytes();
        userInfo.setB2(byt);
        //userInfo.setB2Str("liuchao");
        UserSPUtils.setParam(getContext(), SPConstant.SPNAME_USER_INFO, SPConstant.SPKEY_USER_INFO, userInfo);

        UserInfo u = GsonUtils.getInstance().fromJson(UserSPUtils.getParam(getContext(), SPConstant.SPNAME_USER_INFO, SPConstant.SPKEY_USER_INFO), UserInfo.class);
        String s = null;*/
    }

    /*public void test2() {
        SPUtils.clearSP(getContext(), SPConstant.SPNAME_USER_INFO);
    }

    public void test3() {
        *//**
         * default值应为存储对象的JSON字符串
     *//*
        User user = GsonUtils.getInstance().fromJson(SPUtils.getParam(getContext(), SPConstant.SPNAME_USER_INFO, SPConstant.SPKEY_USER_INFO), User.class);
    }

    public void test4() {
        SPUtils.setParam(getContext(), SPConstant.SPNAME_USER_INFO, null, "");
    }


    public void test5() {
        User user = GsonUtils.getInstance().fromJson("{}", User.class);
    }

    public void test6() {
        SPUtils.setParam(getContext(), SPConstant.SPNAME_USER_INFO, SPConstant.SPKEY_USER_INFO, 1);
        int test = GsonUtils.getInstance().fromJson(SPUtils.getParam(getContext(), SPConstant.SPNAME_USER_INFO, SPConstant.SPKEY_USER_INFO), Integer.class);
    }


    // 内部类，用于测试
    class User {
        public String id;
        public int age;
        public String name;

        public String getId() {
            return id;
        }

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setName(String name) {
            this.name = name;
        }
    }*/
}
