package com.youyou.uuelectric.renter.Network;

import java.nio.charset.Charset;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 客户端接入层做加解密
 *
 * @author yangshangdi 2014年8月13日 下午6:43:05
 */
public class AESUtils {

    private static final String MD5 = "MD5";

    private static Charset utf8 = Charset.forName("utf-8");
    /**
     * 固定向量，和客户端协商写死的
     */
    private static AlgorithmParameterSpec PARAM_SPEC = new IvParameterSpec("uuvr-rdm-ahi-815".getBytes(utf8));
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    /**
     * 128 位
     */
    private static final int KEY_SIZE = 16;

    public static byte[] encrypt(byte[] key, byte[] data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(encode(key, KEY_SIZE), "AES");
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);// 创建密码器

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, PARAM_SPEC);// 初始化
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt(byte[] key, byte[] data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(encode(key, KEY_SIZE), "AES");
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, keySpec, PARAM_SPEC);// 初始化
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 用MD5算法加密字节数组
     *
     * @param bytes 要加密的字节
     * @return byte[] 加密后的字节数组，若加密失败，则返回null
     */
    public final static byte[] encode(byte[] bytes) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(bytes);
            byte[] digesta = digest.digest();
            return digesta;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param
     * @param length 截取加密后16字节的多少字节数
     * @return 如果加密失败，返回null
     */
    public final static byte[] encode(byte[] content, int length) {

        byte[] result = encode(content);
        if (result != null) {
            if (result.length > length) {
                byte[] r = new byte[length];
                System.arraycopy(result, 0, r, 0, length);
                return r;
            } else {
                return result;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {

        byte[] key = "uu-key-test".getBytes(utf8);
        byte[] content = "uu-content-test".getBytes(utf8);
        byte[] output = encrypt(key, content);
        byte[] afterDecrypt = decrypt(key, output);
    }

}
