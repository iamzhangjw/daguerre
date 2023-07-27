package pers.zjw.daguerre;

import pers.zjw.daguerre.oss.AliyunApi;
import pers.zjw.daguerre.oss.OssHolder;
import pers.zjw.daguerre.oss.domain.FileMetadata;
import pers.zjw.daguerre.pojo.entity.Credential;
import pers.zjw.daguerre.service.CredentialService;
import pers.zjw.daguerre.utils.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * AliApiTest
 *
 * @author zhangjw
 * @date 2022/10/07 0007 16:03
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DaguerreApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AliApiTest {
    @Autowired
    private CredentialService credentialService;
    private AliyunApi api;

    @Before
    public void setup() {
        Credential credential = credentialService.getByAccessKey("Sh30KY2JCR919d");
        OssHolder.put(credential);
        api = (AliyunApi)OssHolder.getApi();
    }

    @Test
    public void testCreateBucket() {
        String bucket = "abc12345";
        if (!api.bucketExists(bucket)) {
            api.createBucket(bucket);
        }
    }

    @Test
    public void testObject() {
        FileMetadata metadata = api.metadata("test-zjw3", "week/doc/01FZG2RGWJ6F6XZXFSE9E1QABE.pdf");
        System.out.println(JsonParser.toString(metadata));
    }
}
