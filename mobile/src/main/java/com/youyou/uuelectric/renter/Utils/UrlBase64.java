package com.youyou.uuelectric.renter.Utils;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by liuchao on 2015/9/7.
 */
public class UrlBase64 {

    public static final String decode(String str) {
        if (str != null && !"".equals(str)) {
            String temp = str.replace("-", "+").replace("_", "/").replace("~", "=");

            byte[] byt = Base64.decodeBase64(temp.getBytes());
            //System.out.println(new String(byt,"UTF-8"));
            try {
                return URLDecoder.decode(new String(byt, "UTF-8"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
