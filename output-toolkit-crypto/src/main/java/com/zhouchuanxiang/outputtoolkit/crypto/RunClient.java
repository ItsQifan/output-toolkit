package com.zhouchuanxiang.outputtoolkit.crypto;

import com.zhouchuanxiang.outputtoolkit.crypto.manager.ZipCryptoManager;
import com.zhouchuanxiang.outputtoolkit.crypto.util.AESUtil;
import com.zhouchuanxiang.outputtoolkit.crypto.util.ZipCryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Scanner;

/**
 * @Author zhouchuanxiang
 * @Description  加密  解密  ！！！ 重要！！！
 * @Date 15:07 2025/11/11
 * @Param
 * @return
 **/
@Slf4j
public class RunClient {


    public static void main(String[] args) throws Exception {

        String sourceFile="D:\\0001新代码路径\\riskproduct\\tmp_code\\tmp_code.zip";
        //加密后的文件名字，  可不带后缀
        String zipFile="D:\\test\\HttpToolkit-9.20.9.exe";

        String afterEncryptFile="D:\\test\\yuan";

//        encrypt(sourceFile, zipFile);
        decrypt(zipFile, afterEncryptFile);
    }

    /**
     * @Author zhouchuanxiang
     * @Description  简单方式
     * 控制台输入交互方式
     * 加密文件
     * @Date 15:34 2025/11/11
     * @Param [zipFile, afterEncryptFile]
     * @return
     **/
    private static void decrypt(String zipFile, String afterEncryptFile) throws Exception {
        String pwd="";
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入解密密码：");
        pwd = scanner.nextLine();
        if(StringUtils.isBlank(pwd)){
            throw new RuntimeException("密码不能为空");
        }
        long startTime = System.currentTimeMillis();
//        log.info("输入的解密密码是:{}",pwd);
//        pwd= AESUtil.decrypt(pwd);
        ZipCryptoUtil.decryptZip(zipFile, afterEncryptFile, pwd);
        System.out.println("解密成功！执行完毕,执行时间："+(System.currentTimeMillis()- startTime) /1000 +"s");
    }

    /**
     * @Author zhouchuanxiang
     * @Description  简单方式
     * 控制台输入交互方式
     * 解密文件
     * @Date 15:34 2025/11/11
     * @Param [zipFile, afterEncryptFile]
     * @return
     **/
    private static void encrypt(String sourceFile, String zipFile) throws Exception {
        String pwd="";
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入加密密码：");
        pwd = scanner.nextLine();
        if(StringUtils.isBlank(pwd)){
            throw new RuntimeException("密码不能为空");
        }
        log.info("加密密码为，请复制:{}",AESUtil.encrypt(pwd));
        long startTime = System.currentTimeMillis();
        ZipCryptoUtil.encryptZip(sourceFile, zipFile,pwd);

        System.out.println("加密成功！执行完毕,执行时间："+(System.currentTimeMillis()- startTime) /1000 +"s");
    }
}
