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
public class StorageService4MinioTest {
    @Autowired
    private StorageService fileService;
    @Autowired
    private CredentialService credentialService;

    @Before
    public void setup() {
        Credential credential = credentialService.getByAccessKey("bzThOgtPBJ4cd5");
        OssHolder.put(credential);
    }

    @Test
    public void testException() {
        fileService.url("123", 10);
    }

    @Test
    public void testSmallFileUpload() throws IOException {
        Path path = Paths.get("E:\\downloads\\V2.6.2_436_ZJDQ_F1p_20210623.apk");
        String filename = "V2.6.2_436_ZJDQ_F1p_20210623.apk";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        upload.setExpireDays(2);
        FileUploadSchedule schedule = fileService.upload(file, upload);
        System.out.println(fileService.url(schedule.getFileId(), 120));
    }

    @Test
    public void testLargeFileUpload() throws IOException {
        Path path = Paths.get("E:\\downloads\\android-studio-4-2-2.exe");
        String filename = "android-studio-4-2-2.exe";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        upload.setExpireDays(7);
        FileUploadSchedule schedule = fileService.upload(file, upload);
        System.out.println(fileService.url(schedule.getFileId(), 120));
    }

    @Test
    public void testIntermittentTransmissionWithWholeFile() throws IOException {
        Path path = Paths.get("E:\\downloads\\android-studio-4-2-2.exe");
        String filename = "android-studio-4-2-2.exe";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        upload.setFileId("01FZFRQD452KEFTQRTQ8W6TGB1");
        upload.setIndex(0);
        fileService.upload(file, upload);
    }

    @Test
    public void testIntermittentTransmissionWithChunkFile() throws IOException {
        Path path = Paths.get("E:\\downloads\\ukn_88735774528212992.zip.2");
        String filename = "ukn_88735774528212992.zip.2";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        upload.setFileId("01FX2D7C17157M1EW60CPFMMPF");
        upload.setIndex(1);
        fileService.upload(file, upload);
    }

    @Test
    public void testFirstChunkUpload() throws IOException {
        Path path = Paths.get("E:\\downloads\\ukn_88735774528212992.zip.1");
        String filename = "ukn_88735774528212992.zip.1";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        upload.setFilename("test.zip");
        upload.setLength(161240177L);
        upload.setChunkSize(52428800);
        fileService.upload(file, upload);
    }

    @Test
    public void testImageFileUpload() throws IOException {
        Path path = Paths.get("E:\\downloads\\88947770943049728.jpg");
        String filename = "eva.jpg";
        MultipartFile file = new MockMultipartFile(filename, filename, "", new FileInputStream(path.toFile()));
        FileUpload upload = new FileUpload();
        upload.setExpireDays(2);
        fileService.upload(file, upload);
    }
}
