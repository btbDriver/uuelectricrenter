package com.youyou.uuelectric.renter.Network.exception;

import com.android.volley.VolleyError;

/**
 * Created by taurusxi on 14-9-7.
 */
public class NetWorkFallException extends VolleyError
{
    public NetWorkFallException() {
        super();
    }
    public NetWorkFallException(String error) {
        super(error);
    }
}
