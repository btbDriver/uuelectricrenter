package com.youyou.uuelectric.renter.Network;

/**
 * Created by taurusxi on 14-8-6.
 */
public interface HttpNetwork<T> {


    public abstract void doPost(int seq, NetworkTask networkTask, HttpResponse.NetWorkResponse response);


}
