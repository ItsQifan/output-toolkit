package com.zhouchuanxiang.outputtoolkit.crypto.util;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import java.io.File;

/**
 * @Author zhouchuanxiang
 * @Description  zip加密解密工具类
 * @Date 14:23 2025/11/11
 * @Param
 * @return
 **/
public class ZipCryptoUtil {


    /**
     * @Author zhouchuanxiang
     * @Description  加密方法
     * @Date 14:23 2025/11/11
     * @Param [sourceFile, zipFile, password]
     * sourceFile：源zip文件路径，或者文件夹
     * zipFile：压缩后文件的名字
     * @return
     **/
    public static void encryptZip(String sourceZipFile, String targetZipFile, String password) throws Exception {
        // 创建ZipFile对象，指定密码
        try (ZipFile zip = new ZipFile(targetZipFile, password.toCharArray())) {
            // 配置压缩参数：AES-256加密
            ZipParameters parameters = new ZipParameters();
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES); // 使用AES加密
            parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256); // 256位密钥（最高强度）

            // 添加文件到ZIP（支持单个文件或目录）
            zip.addFile(new File(sourceZipFile), parameters);
        }
    }

    /**
     * @Author zhouchuanxiang
     * @Description  解密
     * @Date 14:25 2025/11/11
     * @Param [zipFile, destDir, password]
     * zipFile：要解压的zip文件
     * destDir：解压到目标目录
     * @return
     **/
    public static void decryptZip(String zipFile, String destDir, String password) throws Exception {
        try (ZipFile zip = new ZipFile(zipFile)) {
            if (zip.isEncrypted()) {
                zip.setPassword(password.toCharArray()); // 设置解密密码
            }
            // 解压到目标目录
            zip.extractAll(destDir);
        }
    }
}
