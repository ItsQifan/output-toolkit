package com.zhouchuanxiang.outputtoolkit.crypto.manager;

import com.zhouchuanxiang.outputtoolkit.crypto.util.ZipCryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
/**
 * @Author zhouchuanxiang
 * @Description  加密解密zip
 * @Date 14:17 2025/11/11
 * @Param
 * @return
 **/
@Component
@Slf4j
public class ZipCryptoManager {


    /**
     * @Author zhouchuanxiang
     * @Description
     * @Date 14:34 2025/11/11
     * @Param [sourceFile, zipFile, password]
     * @return
     **/
    public boolean encryptZip(String sourceFile, String zipFile, String password) throws Exception {
        long time = System.currentTimeMillis();
        log.info("开始加密zip文件：sourceFile:{},zipFile:{}", sourceFile,zipFile);
        ZipCryptoUtil.encryptZip(sourceFile, zipFile, password);
        log.info("加密zip完成,耗时：:{}ms", System.currentTimeMillis() - time);
        return true;
    }

}
