package pers.zjw.daguerre.service;

import pers.zjw.daguerre.oss.OssApi;
import pers.zjw.daguerre.oss.OssHolder;
import pers.zjw.daguerre.oss.domain.ChunkFileMetadata;
import pers.zjw.daguerre.oss.domain.FileMetadata;
import pers.zjw.daguerre.oss.domain.ObjectName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * minio service
 *
 * @author zhangjw
 * @date 2022/4/1 0001 17:55
 */
@Slf4j
@Service
public class OssService {
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private OssApi api() {
        return OssHolder.getApi();
    }

    @PreDestroy
    protected void close() {
        OssHolder.close();
    }

    public FileMetadata upload(String bucket, ObjectName objectName, Path path) {
        return upload(bucket, objectName, path, -1);
    }

    public FileMetadata upload(String bucket, ObjectName objectName, Path path, long length) {
        return api().upload(bucket, objectName, path, length);
    }

    public FileMetadata upload(String bucket, ObjectName objectName, InputStream stream) {
        return upload(bucket, objectName, stream, -1);
    }

    public FileMetadata upload(String bucket, ObjectName objectName, InputStream stream, long length) {
        return api().upload(bucket, objectName, stream, length);
    }

    public FileMetadata upload(FileMetadata metadata, InputStream stream) {
        return api().upload(metadata.getBucketName(), metadata.toObjectName(), stream, metadata.getLength());
    }

    public FileMetadata upload(FileMetadata metadata, java.io.File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return upload(metadata, fis);
        }
    }

    public ChunkFileMetadata chunkUpload(FileMetadata metadata, InputStream stream, int index, long length) {
        return api().chunkUpload(metadata.getBucketName(), metadata.toObjectName(), stream, index, length);
    }

    public ChunkFileMetadata chunkUpload(FileMetadata metadata, java.io.File file, int index) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return chunkUpload(metadata, fis, index, file.length());
        }
    }

    public String startMultipartUpload(FileMetadata metadata) {
        return api().startMultiChunkUpload(metadata.getBucketName(), metadata.toObjectName());
    }

    public FileMetadata completeMultiChunkUpload(String bucket, ObjectName objectName, List<ChunkFileMetadata> metadata) {
        return api().completeMultiChunkUpload(bucket, objectName, metadata);
    }

    public String url(String bucket, String objectName, int expireMinutes) {
        return api().url(bucket, objectName, expireMinutes);
    }

    public InputStream download(String bucket, String objectName, long offset, long length) {
        return api().download(bucket, objectName, offset, length);
    }

    public void remove(String bucket, String objectName) {
        api().delete(bucket, objectName);
    }

    public void remove(String bucket, Collection<String> objectNames) {
        OssApi api = api();
        taskExecutor.submit(() -> api.delete(bucket, objectNames));
    }
}
