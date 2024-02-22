package com.atguigu.gulimall.thirdparty.oss;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
@SpringBootTest
public class OssTest {

    @Autowired
    OSSClient ossClient;

    @Test
    public void upload() throws Exception {
//        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "";666
//        String accessKeySecret = "";
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 填写Bucket名称，例如examplebucket。
        String bucketName = "gulimall-zxy-01";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "E:/Desktop/猫.jpg";
        FileInputStream inputStream = new FileInputStream(objectName);
        ossClient.putObject(bucketName,"猫2.jpg",inputStream);
        ossClient.shutdown();
        System.out.println("上传完成");
    }
}
