package com.youyou.uuelectric.renter.Utils.mina;

import com.uu.access.app.header.HeaderCommon;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * User: qing
 * Date: 2015-07-01 11:54
 * Desc: 对服务端下发的响应字节进行解码，转换成客户端处理的响应对象实体
 */
public class LongConnectResponseDecoder extends CumulativeProtocolDecoder {

    private static final String TAG = LongConnectResponseDecoder.class.getSimpleName();

    @Override
    protected boolean doDecode(IoSession session, IoBuffer ioBuffer, ProtocolDecoderOutput out) throws Exception {
        if (ioBuffer.prefixedDataAvailable(4)) {
            int serverLength = ioBuffer.getInt();
            byte[] bytes = new byte[serverLength];
            ioBuffer.get(bytes);
            HeaderCommon.ResponsePackage responsePackage = HeaderCommon.ResponsePackage.parseFrom(bytes);
            out.write(responsePackage);
            return true;
        }
        return false;
    }
}
