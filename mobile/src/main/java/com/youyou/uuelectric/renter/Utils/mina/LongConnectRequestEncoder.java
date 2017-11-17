package com.youyou.uuelectric.renter.Utils.mina;


import com.uu.access.app.header.HeaderCommon;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * User: qing
 * Date: 2015-07-01 11:53
 * Desc: 对长连接的请求对象进行字节编码
 */
public class LongConnectRequestEncoder implements ProtocolEncoder {
    @Override
    public void encode(IoSession ioSession, Object object, ProtocolEncoderOutput out) throws Exception {
        HeaderCommon.RequestPackage requestPackage = (HeaderCommon.RequestPackage) object;
        byte[] reqData = requestPackage.toByteArray();
        IoBuffer requestBuffer = IoBuffer.allocate(4 + reqData.length, false);
        requestBuffer.putInt(reqData.length);
        requestBuffer.put(reqData);
        requestBuffer.flip();
        out.write(requestBuffer);
    }

    @Override
    public void dispose(IoSession ioSession) throws Exception {

    }


}
