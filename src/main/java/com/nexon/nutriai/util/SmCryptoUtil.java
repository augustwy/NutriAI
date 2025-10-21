package com.nexon.nutriai.util;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

/**
 * 国密算法工具类
 *
 * 提供中国国家密码管理局发布的国产密码算法实现：
 * - SM2: 椭圆曲线公钥密码算法，用于数字签名和密钥交换
 * - SM3: 密码杂凑算法，用于生成数据摘要
 * - SM4: 分组密码算法，用于对称加密
 *
 * 依赖 Bouncy Castle 安全提供者实现
 */
public class SmCryptoUtil {

    static {
        // 添加BouncyCastleProvider支持
        Security.addProvider(new BouncyCastleProvider());
    }

    // SM4算法常量
    private static final String SM4_ALGORITHM = "SM4";
    private static final String SM4_ECB_PADDING = "SM4/ECB/PKCS5Padding";
    private static final String SM4_CBC_PADDING = "SM4/CBC/PKCS5Padding";
    private static final int SM4_KEY_SIZE = 16;

    // SM2算法常量
    private static final String SM2_ALGORITHM = "SM2";
    private static final int SM2_SIGN_LENGTH = 64;

    /**
     * SM4 ECB模式加密
     *
     * @param data 明文数据
     * @param key  密钥（16字节）
     * @return 密文数据（Base64编码）
     */
    public static String sm4EncryptECB(String data, byte[] key) throws Exception {
        if (key.length != SM4_KEY_SIZE) {
            throw new IllegalArgumentException("SM4密钥长度必须为16字节");
        }

        Cipher cipher = Cipher.getInstance(SM4_ECB_PADDING, "BC");
        Key sm4Key = new SecretKeySpec(key, SM4_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, sm4Key);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * SM4 ECB模式解密
     *
     * @param encryptedData 密文数据（Base64编码）
     * @param key           密钥（16字节）
     * @return 明文数据
     */
    public static String sm4DecryptECB(String encryptedData, byte[] key) throws Exception {
        if (key.length != SM4_KEY_SIZE) {
            throw new IllegalArgumentException("SM4密钥长度必须为16字节");
        }

        Cipher cipher = Cipher.getInstance(SM4_ECB_PADDING, "BC");
        Key sm4Key = new SecretKeySpec(key, SM4_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, sm4Key);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * SM4 CBC模式加密
     *
     * @param data 明文数据
     * @param key  密钥（16字节）
     * @param iv   初始化向量（16字节）
     * @return 密文数据（Base64编码）
     */
    public static String sm4EncryptCBC(String data, byte[] key, byte[] iv) throws Exception {
        if (key.length != SM4_KEY_SIZE) {
            throw new IllegalArgumentException("SM4密钥长度必须为16字节");
        }
        if (iv.length != SM4_KEY_SIZE) {
            throw new IllegalArgumentException("SM4 IV长度必须为16字节");
        }

        Cipher cipher = Cipher.getInstance(SM4_CBC_PADDING, "BC");
        Key sm4Key = new SecretKeySpec(key, SM4_ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, sm4Key, ivParameterSpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * SM4 CBC模式解密
     *
     * @param encryptedData 密文数据（Base64编码）
     * @param key           密钥（16字节）
     * @param iv            初始化向量（16字节）
     * @return 明文数据
     */
    public static String sm4DecryptCBC(String encryptedData, byte[] key, byte[] iv) throws Exception {
        if (key.length != SM4_KEY_SIZE) {
            throw new IllegalArgumentException("SM4密钥长度必须为16字节");
        }
        if (iv.length != SM4_KEY_SIZE) {
            throw new IllegalArgumentException("SM4 IV长度必须为16字节");
        }

        Cipher cipher = Cipher.getInstance(SM4_CBC_PADDING, "BC");
        Key sm4Key = new SecretKeySpec(key, SM4_ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, sm4Key, ivParameterSpec);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * SM3哈希计算
     *
     * @param data 输入数据
     * @return 32字节哈希值（十六进制字符串）
     */
    public static String sm3Hash(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        SM3Digest sm3Digest = new SM3Digest();
        sm3Digest.update(dataBytes, 0, dataBytes.length);
        byte[] hash = new byte[sm3Digest.getDigestSize()];
        sm3Digest.doFinal(hash, 0);
        return bytesToHex(hash);
    }

    /**
     * SM2生成密钥对
     *
     * @return SM2密钥对
     */
    public static KeyPair sm2GenerateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        // 使用国密标准的椭圆曲线参数
        ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
        keyPairGenerator.initialize(sm2Spec);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * SM2公钥加密
     *
     * @param data      明文数据
     * @param publicKey 公钥
     * @return 密文数据（Base64编码）
     */
    public static String sm2Encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(SM2_ALGORITHM, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * SM2私钥解密
     *
     * @param encryptedData 密文数据（Base64编码）
     * @param privateKey    私钥
     * @return 明文数据
     */
    public static String sm2Decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(SM2_ALGORITHM, "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * SM2签名
     *
     * @param data       待签名数据
     * @param privateKey 私钥
     * @return 签名值（Base64编码）
     */
    public static String sm2Sign(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SM2Sign", "BC");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signBytes);
    }

    /**
     * SM2验签
     *
     * @param data      待验证数据
     * @param sign      签名值（Base64编码）
     * @param publicKey 公钥
     * @return 验证结果
     */
    public static boolean sm2Verify(String data, String sign, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SM2Sign", "BC");
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.getDecoder().decode(sign));
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] secretBytes = new byte[32]; // 256位
        random.nextBytes(secretBytes);
        return Base64.getEncoder().encodeToString(secretBytes);
    }

    static void main() {
        try {
            String s = SmCryptoUtil.generateSecret();
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
