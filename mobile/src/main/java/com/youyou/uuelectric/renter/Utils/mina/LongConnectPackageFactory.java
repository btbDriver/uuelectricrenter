package com.youyou.uuelectric.renter.Utils.mina;

import android.content.Context;

import com.google.protobuf.ByteString;
import com.uu.access.app.header.HeaderCommon;
import com.uu.access.longconnection.pb.LongConnection;
import com.uu.facade.base.cmd.Cmd;
import com.youyou.uuelectric.renter.Network.AESUtils;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Utils.Support.Config;

/**
 * User: qing
 * Date: 2015-07-04 18:08
 * Desc:
 */
public class LongConnectPackageFactory {


    /**
     * 创建长连接握手协议包对象
     *
     * @return
     */
    public static HeaderCommon.RequestPackage creatLongBytePacage(Context context) {
        HeaderCommon.CommonReqHeader.Builder reqHeaderBuilder = HeaderCommon.CommonReqHeader.newBuilder();
        reqHeaderBuilder.setCmd(Cmd.CmdCode.EstablishLongConnection_NL_VALUE);
        reqHeaderBuilder.setSeq(NetworkUtils.seq.getAndIncrement());
        reqHeaderBuilder.setUa(NetworkTask.DEFEALT_UA);
        reqHeaderBuilder.setB2(ByteString.copyFrom(UserConfig.getUserInfo().getB2()));
        reqHeaderBuilder.setUuid(UserConfig.getUUID());
        LongConnection.EstablishLongConnection.Request.Builder longConnect = LongConnection.EstablishLongConnection.Request.newBuilder();
        longConnect.setNetType(Config.getNetworkType(context) == Config.NETWORKTYPE_INVALID ? Config.NETWORKTYPE_2G : Config.getNetworkType(context));
        HeaderCommon.RequestData.Builder reqDataBuilder = HeaderCommon.RequestData.newBuilder();
        reqDataBuilder.setHeader(reqHeaderBuilder.build());
        reqDataBuilder.setBusiData(longConnect.build().toByteString());
        HeaderCommon.RequestPackage.Builder requestPackageBuilder = HeaderCommon.RequestPackage.newBuilder();
        requestPackageBuilder.setB3(ByteString.copyFrom(UserConfig.getUserInfo().getB3()));
        requestPackageBuilder.setSessionKey(ByteString.copyFrom(UserConfig.getUserInfo().getSessionKey()));
        requestPackageBuilder.setReqData(ByteString.copyFrom(AESUtils.encrypt(UserConfig.getUserInfo().getB3Key(), reqDataBuilder.build().toByteArray())));

        return requestPackageBuilder.build();

    }


    /**
     * 创建长连接心跳请求包
     *
     * @return
     */
    public static HeaderCommon.RequestPackage createHeartBeatPackage() {
        try {
            HeaderCommon.CommonReqHeader.Builder reqHeaderBuilder = HeaderCommon.CommonReqHeader.newBuilder();
            reqHeaderBuilder.setCmd(Cmd.CmdCode.HeartBeat_NL_VALUE);
            reqHeaderBuilder.setSeq(NetworkUtils.seq.getAndIncrement());
            reqHeaderBuilder.setUa(NetworkTask.DEFEALT_UA);
            if (UserConfig.getUserInfo().getB2() == null) {
                return null;
            }
            reqHeaderBuilder.setB2(ByteString.copyFrom(UserConfig.getUserInfo().getB2()));
            reqHeaderBuilder.setUuid(UserConfig.getUUID());
            LongConnection.HeartBeat.Request.Builder longConnect = LongConnection.HeartBeat.Request.newBuilder();
            HeaderCommon.RequestData.Builder reqDataBuilder = HeaderCommon.RequestData.newBuilder();
            reqDataBuilder.setHeader(reqHeaderBuilder.build());
            reqDataBuilder.setBusiData(longConnect.build().toByteString());
            HeaderCommon.RequestPackage.Builder requestPackageBuilder = HeaderCommon.RequestPackage.newBuilder();
            if (UserConfig.getUserInfo().getB3() == null) {
                return null;
            }
            requestPackageBuilder.setB3(ByteString.copyFrom(UserConfig.getUserInfo().getB3()));
            if (UserConfig.getUserInfo().getSessionKey() == null) {
                return null;
            }
            requestPackageBuilder.setSessionKey(ByteString.copyFrom(UserConfig.getUserInfo().getSessionKey()));
            if (UserConfig.getUserInfo().getB3Key() == null) {
                return null;
            }
            requestPackageBuilder.setReqData(ByteString.copyFrom(AESUtils.encrypt(UserConfig.getUserInfo().getB3Key(), reqDataBuilder.build().toByteArray())));

            return requestPackageBuilder.build();
        } catch (Exception e) {
            return null;
        }
    }
}
