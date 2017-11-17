package com.youyou.uuelectric.renter.Utils.mina;

import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.access.app.header.HeaderCommon;
import com.uu.facade.base.cmd.Cmd;
import com.youyou.uuelectric.renter.Network.AESUtils;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

/**
 * User: qing
 * Date: 2015-07-01 11:51
 * Desc: 客户端和服务器端进行心跳发送和响应的心跳工厂类
 */
public class ClientKeepAliveMessageFactory implements KeepAliveMessageFactory {

    private static final String TAG = ClientKeepAliveMessageFactory.class.getSimpleName();


    @Override
    public boolean isRequest(IoSession ioSession, Object object) {
        return false;
    }

    @Override
    public boolean isResponse(IoSession ioSession, Object object) {
        if (object != null && object instanceof HeaderCommon.ResponsePackage) {
            HeaderCommon.ResponsePackage responsePackage = (HeaderCommon.ResponsePackage) object;
            if (responsePackage.getRet() == 0) {
                HeaderCommon.ResponseData responseData = null;
                try {
                    responseData = HeaderCommon.ResponseData.parseFrom(AESUtils.decrypt(UserConfig.getUserInfo().getB3Key(), responsePackage.getResData().toByteArray()));
                    int cmd = responseData.getCmd();
                    // 接收到响应后，判断响应是否是心跳响应，如果是心跳则直接返回true，消息不会再传递到Handler
                    if (cmd == Cmd.CmdCode.HeartBeat_NL_VALUE) {
                        L.i("心跳response成功返回...");
                        return true;
                    } else { // 其他响应则返回false，由Handler去处理
                        return false;
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    @Override
    public Object getRequest(IoSession ioSession) {
        return LongConnectPackageFactory.createHeartBeatPackage();
    }

    @Override
    public Object getResponse(IoSession ioSession, Object o) {
        return null;
    }
}
