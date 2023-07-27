package pers.zjw.daguerre;

import pers.zjw.daguerre.constant.FileType;
import pers.zjw.daguerre.oss.MinioApi;
import pers.zjw.daguerre.oss.OssHolder;
import pers.zjw.daguerre.oss.domain.ChunkFileMetadata;
import pers.zjw.daguerre.oss.domain.ObjectName;
import pers.zjw.daguerre.oss.domain.ObjectNameFactory;
import pers.zjw.daguerre.pojo.entity.Credential;
import pers.zjw.daguerre.service.CredentialService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * minio api test
 *
 * @author zhangjw
 * @date 2022/05/31 0031 16:43
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DaguerreApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MinioApiTest {
    @Autowired
    private CredentialService credentialService;
    private MinioApi api;
    private ObjectNameFactory objectNameFactory;

    @Before
    public void setup() {
        objectNameFactory = new ObjectNameFactory("uuid");
        Credential credential = credentialService.getByAccessKey("bzThOgtPBJ4cd5");
        OssHolder.put(credential);
        api = (MinioApi)OssHolder.getApi();
    }

    @Test
    public void testOthers() {
        String bucketName = "test";

        System.out.println(api.exists(bucketName, "img/87966619642335232.jpg"));
        //System.out.println(api.metadata(bucketName, "img/87966619642335232.jpg"));
    }

    @Test
    public void testUpload() throws IOException {
        String bucketName = "asiatrip";
        FileType fileType = FileType.EXE;
        String filename = "android-sdk_r24.4.1-windows.zip";
        Path path = Paths.get("E:\\downloads\\android-sdk_r24.4.1-windows.zip");
        String fileId = "1234567";
        int bufferSize = 1024*1024, bufferCount = 50, readSize = 0, i = 0;
        byte[] buffer = new byte[bufferSize];
        String tmpFile = "E:\\downloads\\" + filename + "." + i;
        OutputStream os = new FileOutputStream(tmpFile);
        File file = path.toFile();
        System.out.println(file.length());
        FileInputStream fis = new FileInputStream(file);
        List<ChunkFileMetadata> chunks = new ArrayList<>();
        int read = 0, chunkRead = 0;
        ObjectName objectName = objectNameFactory.create(filename, FileType.UNKNOWN);
        api.startMultiChunkUpload(bucketName, objectName);
        while((read = fis.read(buffer)) > 0) {
            os.write(buffer, 0, read);
            i++;
            readSize += read;
            chunkRead += read;
            if (i % bufferCount == 0 || readSize >= file.length()) {
                os.flush();
                os.close();
                ChunkFileMetadata metadata = api.chunkUpload(bucketName, objectName, new FileInputStream(tmpFile), i, chunkRead);
                chunkRead = 0;
                System.out.println(metadata);
                chunks.add(metadata);
                if (readSize < file.length()) {
                    tmpFile = "E:\\downloads\\" + filename + "." + i;
                    os = new FileOutputStream(tmpFile);
                }
            }
        }
        System.out.println(api.completeMultiChunkUpload(bucketName, objectName, chunks));
    }
}
