package com.youyou.uuelectric.renter.Network;


import com.youyou.uuelectric.renter.UUApp;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by taurusxi on 14-8-6.
 */
public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static final AtomicInteger seq = new AtomicInteger(100);
    public static final AtomicBoolean isShowDialog = new AtomicBoolean(false);

    /**
     * 执行网络请求
     *
     * @param networkTask
     * @param baseResponse
     */
    public static void executeNetwork(NetworkTask networkTask, HttpResponse.NetWorkResponse baseResponse) {
        HttpNetwork httpNetwork = new VolleyNetworkHelper();
        switch (networkTask.getMethod()) {
            case NetworkTask.Method.POST:
                httpNetwork.doPost(seq.incrementAndGet(), networkTask, baseResponse);
                break;
            default:
                break;
        }
    }

    /**
     * 取消网络请求
     * @param tag
     */
    public static void cancleNetworkRequest(Object tag) {
        UUApp.getRequestQueue().cancelAll(tag);
    }
}
