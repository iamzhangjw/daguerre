package pers.zjw.daguerre.oss;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import pers.zjw.daguerre.exception.OssException;
import pers.zjw.daguerre.oss.domain.ChunkFileMetadata;
import pers.zjw.daguerre.oss.domain.DeleteResult;
import pers.zjw.daguerre.oss.domain.FileMetadata;
import pers.zjw.daguerre.oss.domain.ObjectName;
import pers.zjw.daguerre.utils.JsonParser;
import pers.zjw.daguerre.utils.NewDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import pers.zjw.daguerre.constant.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AliyunApi
 * @see <a href="https://www.alibabacloud.com/help/zh/product/31815.html">对象存储 OSS</a>
 *
 * @author zhangjw
 * @date 2022/09/10 0010 16:52
 */
@Slf4j
public class AliyunApi extends AbstractOssApi {
    private static final String DEFAULT_REGION = "oss-cn-shenzhen";

    private final OSS client;

    protected AliyunApi(OssProperties properties) {
        Assert.isTrue(vendor() == properties.getVendor(), "mismatched vendor");
        this.client = new OSSClientBuilder().build(properties.getEndpoint(),
                properties.getAccessKey(), properties.getSecretKey());
    }

    @Override
    public void createBucket(String bucket) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        try {
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucket);
            createBucketRequest.setStorageClass(StorageClass.Standard);
            createBucketRequest.setCannedACL(CannedAccessControlList.Private);
            client.createBucket(createBucketRequest);
            SetBucketLifecycleRequest bucketLifecycleRequest = new SetBucketLifecycleRequest(bucket);
            List<LifecycleRule> rules = ExpireMode.ALL.values().stream()
                    .filter(e -> -1 < e.expireDays())
                    .map(this::parseRule)
                    .collect(Collectors.toList());
            bucketLifecycleRequest.setLifecycleRules(rules);
            client.setBucketLifecycle(bucketLifecycleRequest);
        } catch (OSSException | ClientException e) {
            log.error("create bucket {} occurred exception", bucket, e);
            throw new OssException(vendor(), "create bucket failed", bucket, null, null, e);
        }
    }

    private LifecycleRule parseRule(ExpireMode expireMode) {
        return new LifecycleRule(expireMode.name().toLowerCase() + "-Rule",
                expireMode.directory() + "/", LifecycleRule.RuleStatus.Enabled,
                expireMode.expireDays());
    }

    @Override
    public boolean bucketExists(String bucket) {
        Assert.hasText(bucket, "bucket name must not be null");
        try {
            return client.doesBucketExist(bucket);
        } catch (OSSException | ClientException e) {
            log.error("check bucket {} occurred exception", bucket, e);
            return false;
        }
    }

    @Override
    public FileMetadata upload(String bucket, ObjectName objectName, Path path, long length)
            throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.isTrue(Objects.nonNull(objectName), "objectName must not be null");
        Assert.notNull(path, "file path must not be null");
        createBucketIfMissing(bucket);
        long start = System.currentTimeMillis();
        String absolutePath = path.toAbsolutePath().toString();
        try {
            PutObjectRequest request = new PutObjectRequest(bucket, objectName.name(), path.toFile());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setUserMetadata(userMetadata(objectName));
            metadata.setContentType(MimeType.parse(objectName.original(), objectName.type()));
            if (-1 < objectName.expireDays()) {
                metadata.setExpirationTime(NewDateTimeUtils.localDateTime2Date(
                        NewDateTimeUtils.plusDay(LocalDateTime.now(), objectName.expireDays())));
            }
            if (length > 0) {
                metadata.setContentLength(length);
            }
            request.setMetadata(metadata);
            client.putObject(request);
            return metadata(bucket, objectName.name());
        } catch (OSSException | ClientException e) {
            log.error("upload file[bucket={}, absolutePath={}, objectName={}] failed:{}",
                    bucket, absolutePath, objectName.name(), e.getMessage());
            throw new OssException(vendor(), "upload file failed",
                    bucket, objectName.name(), absolutePath, e);
        } finally {
            log.info("upload file {}[{}] to {} cost {}ms.",
                    objectName.original(), objectName.name(), bucket, System.currentTimeMillis() - start);
        }
    }

    @Override
    public FileMetadata upload(String bucket, ObjectName objectName, InputStream stream, long length)
            throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.isTrue(Objects.nonNull(objectName), "objectName must not be null");
        Assert.notNull(stream, "file stream must not be null");
        createBucketIfMissing(bucket);
        long start = System.currentTimeMillis();
        try {
            PutObjectRequest request = new PutObjectRequest(bucket, objectName.name(), stream);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setUserMetadata(userMetadata(objectName));
            metadata.setContentType(MimeType.parse(objectName.original(), objectName.type()));
            if (-1 < objectName.expireDays()) {
                metadata.setExpirationTime(NewDateTimeUtils.localDateTime2Date(
                        NewDateTimeUtils.plusDay(LocalDateTime.now(), objectName.expireDays())));
            }
            if (length > 0) {
                metadata.setContentLength(length);
            }
            request.setMetadata(metadata);
            client.putObject(request);
            return metadata(bucket, objectName.name());
        } catch (OSSException | ClientException e) {
            log.error("upload file[bucket={}, filename={}, objectName={}] failed:{}",
                    bucket, objectName.original(), objectName.name(), e.getMessage());
            throw new OssException(vendor(), "upload file stream failed",
                    bucket, objectName.name(), objectName.original(), e);
        } finally {
            log.info("upload file {}[{}] to {} cost {}ms.", objectName.original(),
                    objectName.name(), bucket, System.currentTimeMillis() - start);
        }
    }

    @Override
    public String startMultiChunkUpload(String bucket, ObjectName objectName) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.isTrue(Objects.nonNull(objectName), "objectName must not be null");
        createBucketIfMissing(bucket);
        try {
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucket, objectName.name());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentDisposition("attachment;filename="+objectName.original());
            metadata.setContentType(MimeType.parse(objectName.original(), objectName.type()));
            if (-1 < objectName.expireDays()) {
                metadata.setExpirationTime(NewDateTimeUtils.localDateTime2Date(
                        NewDateTimeUtils.plusDay(LocalDateTime.now(), objectName.expireDays())));
            }
            metadata.setUserMetadata(userMetadata(objectName));
            request.setObjectMetadata(metadata);
            return client.initiateMultipartUpload(request).getUploadId();
        } catch (OSSException | ClientException e) {
            log.error("start multipart[bucket={}, objectName={}] upload failed:{}",
                    bucket, objectName.name(), e.getMessage());
            throw new OssException(vendor(), "gen file access url failed",
                    null, bucket, objectName.name(), e);
        }
    }

    @Override
    public ChunkFileMetadata chunkUpload(String bucket, ObjectName objectName, Path path, int index, long length)
            throws OssException {
        Assert.notNull(path, "file path must not be null");
        String absolutePath = path.toAbsolutePath().toString();
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            return chunkUpload(bucket, objectName, fis, index, length);
        } catch (IOException e) {
            log.error("upload chunk file[bucket={}, absolutePath={}, objectName={}] failed:{}",
                    bucket, absolutePath, objectName.name(), e.getMessage());
            throw new OssException(vendor(), "upload file failed",
                    bucket, objectName.name(), absolutePath, e);
        }
    }

    @Override
    public ChunkFileMetadata chunkUpload(String bucket, ObjectName objectName, InputStream stream,
                                         int index, long length) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.isTrue(Objects.nonNull(objectName), "objectName must not be null");
        Assert.notNull(stream, "file stream must not be null");
        Assert.isTrue(index > -1, "index must not be negative");
        Assert.isTrue(length > 0, "file length must greater than 0");
        long start = System.currentTimeMillis();
        try {
            UploadPartRequest request = new UploadPartRequest(bucket, objectName.name());
            request.setUploadId(objectName.ossId());
            request.setInputStream(stream);
            request.setPartSize(length);
            request.setPartNumber(index+1);
            UploadPartResult result = client.uploadPart(request);
            return new ChunkFileMetadata(
                    new FileMetadata(objectName.id(), objectName.original(), bucket,
                            objectName.name()+"."+index, length, objectName.type(),
                            1, start, objectName.ossId()),
                    index,
                    JsonParser.customize().convertValue(result.getPartETag(),
                            new TypeReference<Map<String, Object>>() {}));
        } catch (OSSException | ClientException e) {
            log.error("upload chunk file[bucket={}, filename={}, objectName={}] failed:{}",
                    bucket, objectName.original(), objectName.name(), e.getMessage());
            throw new OssException(vendor(), "upload chunk file stream failed",
                    bucket, objectName.name(), objectName.original(), e);
        } finally {
            log.info("upload chunk file {}[{}] to {} cost {}ms.", objectName.original(),
                    objectName.name(), bucket, System.currentTimeMillis() - start);
        }
    }

    @Override
    public FileMetadata completeMultiChunkUpload(String bucket, ObjectName objectName,
                                                 List<ChunkFileMetadata> metadata) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.isTrue(Objects.nonNull(objectName), "objectName must not be null");
        Assert.notEmpty(metadata, "chunk Objects must not be null");
        long start = System.currentTimeMillis();
        try {
            CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(bucket, objectName.name(),
                    objectName.ossId(),
                    metadata.stream().map(ChunkFileMetadata::getExtra)
                            .map(e -> new PartETag(Integer.parseInt(e.get("partNumber").toString()),
                                    e.get("etag").toString(),
                                    Long.parseLong(e.get("partSize").toString()),
                                    Long.valueOf(e.get("partCRC").toString())))
                            .collect(Collectors.toList()));
            client.completeMultipartUpload(request);
            return metadata(bucket, objectName.name());
        } catch (OSSException | ClientException e) {
            log.error("compose file[bucket={}, objectName={}] with {} failed:{}",
                    bucket, objectName.name(), metadata, e.getMessage());
            throw new OssException(vendor(), "splice file failed",
                    bucket, objectName.name(), objectName.original(), e);
        } finally {
            log.info("compose file {} in {} with {} cost {}ms.", objectName.name(), bucket,
                    metadata, System.currentTimeMillis() - start);
        }
    }

    @Override
    public String url(String bucket, String objectName, int expireMinutes) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        try {
            return client.generatePresignedUrl(bucket, objectName,
                    NewDateTimeUtils.localDateTime2Date(LocalDateTime.now().plusMinutes(expireMinutes)))
                    .toString();
        } catch (ClientException e) {
            log.error("gen file[bucket={}, objectName={}] access url failed:{}",
                    bucket, objectName, e.getMessage());
            throw new OssException(vendor(), "gen file access url failed",
                    null, bucket, objectName, e);
        }
    }

    @Override
    public InputStream download(String bucket, String objectName, long offset, long length)
            throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        long start = System.currentTimeMillis();
        try {
            GetObjectRequest request = new GetObjectRequest(bucket, objectName);
            long rangeStart = (offset > -1) ? offset : 0;
            long rangeEnd = (length > -1) ? rangeStart+length-1 : -1;
            request.setRange(rangeStart, rangeEnd);
            return client.getObject(request).getObjectContent();
        } catch (OSSException | ClientException e) {
            log.error("download file[bucket={}, objectName={}] as stream failed:{}",
                    bucket, objectName, e.getMessage());
            throw new OssException(vendor(), "download file stream failed",
                    bucket, objectName, null, e);
        } finally {
            log.info("download file {}[{},{}] in {} cost {}ms.",
                    objectName, offset, length, bucket, System.currentTimeMillis() - start);
        }
    }

    @Override
    public void download(String bucket, String objectName, Path path) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        long start = System.currentTimeMillis();
        String absolutePath = path.toAbsolutePath().toString();
        try {
            client.getObject(new GetObjectRequest(bucket, objectName), path.toFile());
        } catch (OSSException | ClientException e) {
            log.error("download file[bucket={}, objectName={}] to {} failed:{}",
                    bucket, objectName, absolutePath, e.getMessage());
            throw new OssException(vendor(), "download file to local failed",
                    bucket, objectName, absolutePath, e);
        } finally {
            log.info("download file {} in {} to {} cost {}ms.",
                    objectName, bucket, absolutePath, System.currentTimeMillis() - start);
        }
    }

    @Override
    public FileMetadata metadata(String bucket, String objectName) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        try {
            return transferMetadata(bucket, objectName,
                    client.getObjectMetadata(new GenericRequest(bucket, objectName)));
        } catch (OSSException | ClientException | ParseException e) {
            log.error("get file[bucket={}, objectName={}] metadata failed:{}",
                    bucket, objectName, e.getMessage());
            throw new OssException(vendor(), "get file metadata failed",
                    bucket, objectName, null, e);
        }
    }

    private static FileMetadata transferMetadata(String bucket, String objectName,
                                                 ObjectMetadata source) throws ParseException {
        long expireDays = -1;
        if (null != source.getExpirationTime()) {
            expireDays = (source.getExpirationTime().getTime() - source.getLastModified().getTime())
                    / DateTimeConstant.DAY_MILLS;
        }
        Map<String, String> userMetadata = source.getUserMetadata();
        return new FileMetadata(userMetadata.get("id"), userMetadata.get("name"), bucket,
                objectName, source.getContentLength(), FileType.parse(userMetadata.get("name")),
                (int) expireDays, source.getLastModified().getTime());
    }

    @Override
    public boolean delete(String bucket, String objectName) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        try {
            log.info("remove file {} in {}.", objectName, bucket);
            client.deleteObject(bucket, objectName);
            return true;
        } catch (OSSException | ClientException e) {
            log.error("remove file[bucket={}, objectName={}] failed:{}",
                    bucket, objectName, e.getMessage());
        }
        return false;
    }

    @Override
    public Collection<DeleteResult> delete(String bucket, Collection<String> objectNames) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.notEmpty(objectNames, "object name set must not be empty");
        Map<String, DeleteResult> map = new HashMap<>(objectNames.size());
        try {
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucket);
            request.setKeys(objectNames.stream().filter(StringUtils::hasText).collect(Collectors.toList()));
            client.deleteObjects(request).getDeletedObjects().forEach(o ->
                    map.put(bucket+o, new DeleteResult(bucket, o, true)));
        } catch (OSSException | ClientException e) {
            log.error("remove file[bucket={}, objectName={}] failed:{}",
                    bucket, objectNames, e.getMessage());
        }
        objectNames.forEach(e -> map.computeIfAbsent(bucket+e, key ->
                new DeleteResult(bucket, e, false)));
        return map.values();
    }

    @Override
    public boolean exists(String bucket, String objectName) {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        try {
            return client.doesObjectExist(bucket, objectName);
        } catch (OSSException | ClientException e) {
            log.error("check file[bucket={}, objectName={}] exists failed:{}",
                    bucket, objectName, e.getMessage());
        }
        return false;
    }

    @Override
    public Vendor vendor() {
        return Vendor.ALIYUN;
    }

    @Override
    public void close() {
        client.shutdown();
    }
}
