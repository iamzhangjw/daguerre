package pers.zjw.daguerre;

import pers.zjw.daguerre.utils.JsonParser;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.LifecycleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * minio test
 *
 * @author zhangjw
 * @date 2022/05/25 0025 14:18
 */
@RunWith(SpringRunner.class)
public class MinioTest {

    private static MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://172.18.1.101:9000/")
                .credentials("zhangjw", "minio123")
                .build();
    }

    @Test
    public void testUpload() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key.
            MinioClient minioClient = minioClient();

            // Make 'asiatrip' bucket if not exist.
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("asiatrip").build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("asiatrip").build());
            } else {
                System.out.println("Bucket 'asiatrip' already exists.");
            }

            // Upload file to bucket 'asiatrip'.
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("asiatrip")
                            .object("dir/th.jpg")
                            .filename("C:\\Users\\Administrator\\Downloads\\th.jpg")
                            .build());
            System.out.println("upload new file to bucket 'asiatrip' successfully.");
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }
    }

    @Test
    public void testGetObject() throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException, BucketPolicyTooLargeException {
        MinioClient minioClient = minioClient();
        LifecycleConfiguration configuration = minioClient.getBucketLifecycle(GetBucketLifecycleArgs.builder()
                .bucket("test").build());
        System.out.println("bucket lifecycle: " + JsonParser.toString(configuration));
        String policy =  minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket("test").build());
        System.out.println("bucket policy: " + policy);
        StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder()
                        .bucket("test")
                        .object("default/exe/01FZFRQD452KEFTQRTQ8W6TGB1.exe.0")
                        .build());
        System.out.println("object meta: " + JsonParser.toString(response));
    }
}
