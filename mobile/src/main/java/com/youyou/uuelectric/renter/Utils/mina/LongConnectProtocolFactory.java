package com.youyou.uuelectric.renter.Utils.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * User: qing
 * Date: 2015-07-01 12:51
 * Desc: 编解码协议工厂类
 */
public class LongConnectProtocolFactory implements ProtocolCodecFactory {

    private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;

    public LongConnectProtocolFactory() {

        this.encoder = new LongConnectRequestEncoder();
        this.decoder = new LongConnectResponseDecoder();
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
}
