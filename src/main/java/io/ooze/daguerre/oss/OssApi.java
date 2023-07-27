package io.ooze.daguerre.oss;

import io.ooze.daguerre.constant.Vendor;
import io.ooze.daguerre.exception.OssException;
import io.ooze.daguerre.oss.domain.ChunkFileMetadata;
import io.ooze.daguerre.oss.domain.DeleteResult;
import io.ooze.daguerre.oss.domain.FileMetadata;
import io.ooze.daguerre.oss.domain.ObjectName;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * OssApi
 *
 * @author zhangjw
 * @date 2022/08/22 0022 9:20
 */
public interface OssApi {
    /**
     * default chunk size is 10MB
     */
    int DEFAULT_CHUNK_SIZE = 10_485_760;
    /**
     * max chunk size is 50M
     */
    int MAX_CHUNK_SIZE = 52_428_800;

    void createBucket(String bucket) throws OssException;

    boolean bucketExists(String bucket);

    FileMetadata upload(String bucket, ObjectName objectName, Path path, long length) throws OssException;

    FileMetadata upload(String bucket, ObjectName objectName, InputStream stream, long length) throws OssException;

    String startMultiChunkUpload(String bucket, ObjectName objectName) throws OssException;

    ChunkFileMetadata chunkUpload(String bucket, ObjectName objectName, Path path, int index, long length) throws OssException;

    ChunkFileMetadata chunkUpload(String bucket, ObjectName objectName, InputStream stream, int index, long length) throws OssException;

    FileMetadata completeMultiChunkUpload(String bucket, ObjectName objectName, List<ChunkFileMetadata> metadata) throws OssException;

    String url(String bucket, String objectName) throws OssException;

    String url(String bucket, String objectName, int expireMinutes) throws OssException;

    InputStream download(String bucket, String objectName) throws OssException;

    InputStream download(String bucket, String objectName, long offset) throws OssException;

    InputStream download(String bucket, String objectName, long offset, long length) throws OssException;

    void download(String bucket, String objectName, Path path) throws OssException;

    FileMetadata metadata(String bucket, String objectName) throws OssException;

    boolean delete(String bucket, String objectName) throws OssException;

    Collection<DeleteResult> delete(String bucket, Collection<String> objectNames) throws OssException;

    boolean exists(String bucket, String objectName);

    Vendor vendor();

    default void close() {
        // do nothing
    }
}
