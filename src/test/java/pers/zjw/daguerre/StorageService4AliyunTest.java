package pers.zjw.daguerre;

import pers.zjw.daguerre.oss.OssHolder;
import pers.zjw.daguerre.pojo.entity.Credential;
import pers.zjw.daguerre.pojo.vo.FileUpload;
import pers.zjw.daguerre.pojo.vo.FileUploadSchedule;
import pers.zjw.daguerre.service.CredentialService;
import pers.zjw.daguerre.service.StorageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * FileService test
 *
 * @author zhangjw
 * @date 2022/05/02 0002 14:57
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DaguerreApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StorageService4AliyunTest {
    @Autowired
    private StorageService fileService;
    @Autowired
    private CredentialService credentialService;

    @Before
    public void setup() {
        Credential credential = credentialService.getByAccessKey("Sh30KY2JCR919d");
        OssHolder.put(credential);
    }

    @Test
    public void testSmallFileUpload() throws IOException {
        Path path = Paths.get("E:\\downloads\\Action in Java 8.pdf");
        String filename = "Action in Java 8.pdf";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        upload.setExpireDays(7);
        FileUploadSchedule schedule = fileService.upload(file, upload);
        System.out.println(fileService.url(schedule.getFileId(), 120));
    }

    @Test
    public void testLargeFileUpload() throws IOException {
        Path path = Paths.get("E:\\downloads\\android-studio-4-2-2.exe");
        String filename = "android-studio-4-2-2.exe";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        upload.setExpireDays(5);
        fileService.upload(file, upload);
    }

    @Test
    public void testLargeFileCompose() throws IOException {
        Path path = Paths.get("E:\\downloads\\android-studio-4-2-2.exe");
        String filename = "android-studio-4-2-2.exe";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        upload.setFileId("01FXHK4A8JFYA81DCWYSGQDT9D");
        fileService.upload(file, upload);
    }

    @Test
    public void testFileUrl() throws IOException {
        System.out.println(fileService.url("01FXHK4A8JFYA81DCWYSGQDT9D", null));
    }

    @Test
    public void testImageFileUpload() throws IOException {
        Path path = Paths.get("E:\\downloads\\88947770943049728.jpg");
        String filename = "eva.jpg";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        fileService.upload(file, upload);
    }

    @Test
    public void testImageFileUpload2() throws IOException {
        Path path = Paths.get("E:\\downloads\\奥巴马.jpg");
        String filename = "奥巴马.jpg";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        fileService.upload(file, upload);
    }
}