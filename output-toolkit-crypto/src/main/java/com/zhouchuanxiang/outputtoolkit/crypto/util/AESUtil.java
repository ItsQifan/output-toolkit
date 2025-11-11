package com.zhouchuanxiang.outputtoolkit.crypto.util;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;


public class AESUtil {

    // 算法/模式/填充方式（CBC模式需IV，PKCS5Padding是标准填充）
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256; // 256位密钥
    private static final int IV_SIZE = 16; // IV长度固定16字节（AES块大小）

    private static final String KEY_BASE64="Ygt2HxPo+tTAnXVp6o+kFWTHPZKeunEPLVXjGNMUUcE=";

    /**
     * 生成AES密钥（256位），建议保存到安全存储（如KMS）
     */
    public static String generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(KEY_SIZE, new SecureRandom()); // 强随机数生成器
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static void main(String[] args) throws Exception {
        System.out.println(encrypt("hello world"));
        System.out.println(encrypt("hello world"));
        System.out.println(decrypt(encrypt("hello world")));
    }

    /**
     * 加密字符串
     * @param plainText 明文
     * @param   Base64编码的密钥
     * @return 加密后的Base64字符串（格式：IV+密文，用:分隔）
     */
    public static String encrypt(String plainText) throws Exception {

        // 1. 解析密钥
        byte[] keyBytes = Base64.getDecoder().decode(KEY_BASE64);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        // 2. 生成随机IV（每次加密不同，增强安全性）
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 3. 加密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 4. 拼接IV和密文（IV需与密文一起传输，解密时需要）
        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 解密字符串
     * @param encryptedText 加密后的字符串（格式：IV+密文，用:分隔）
     * @param keyBase64  Base64编码的密钥
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedText) throws Exception {
        // 1. 拆分IV和密文
        String[] parts = encryptedText.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("无效的加密字符串格式");
        }
        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);

        // 2. 解析密钥
        byte[] keyBytes = Base64.getDecoder().decode(KEY_BASE64);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        // 3. 解密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
