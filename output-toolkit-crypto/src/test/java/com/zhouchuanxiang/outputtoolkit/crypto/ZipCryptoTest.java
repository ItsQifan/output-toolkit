package com.zhouchuanxiang.outputtoolkit.crypto;

import com.zhouchuanxiang.outputtoolkit.crypto.manager.ZipCryptoManager;
import com.zhouchuanxiang.outputtoolkit.crypto.util.ZipCryptoUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Scanner;

@SpringBootTest
@ContextConfiguration(classes = OutputToolkitCryptoApplication.class)
public class ZipCryptoTest {

    @Autowired
    ZipCryptoManager zipCryptoManager;

    /**
     * @Author zhouchuanxiang
     * @Description  测试加密zip
     * @Date 14:27 2025/11/11
     * @Param []
     * @return
     **/
    @Test
    public void testEncryptZip() throws Exception {

        String pwd="";
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入加密密码：");
        pwd = scanner.nextLine();
        if(StringUtils.isBlank(pwd)){
            throw new RuntimeException("密码不能为空");
        }

        zipCryptoManager.encryptZip("D:\\0001新代码路径\\riskproduct\\tmp_code","codeen.zip",pwd);

        System.out.println("加密成功！执行完毕");

    }
}
