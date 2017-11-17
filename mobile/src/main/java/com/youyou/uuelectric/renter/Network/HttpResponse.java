package com.youyou.uuelectric.renter.Network;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Created by taurusxi on 14-8-6.
 */
public class HttpResponse {


    public interface NetWorkResponse<T> {

        public void onSuccessResponse(T responseData) throws InvalidProtocolBufferException;

        public void onError(VolleyError errorResponse);

        public void networkFinish();

    }

}
