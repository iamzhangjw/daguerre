package io.ooze.daguerre.oss;

import io.ooze.daguerre.exception.OssException;
import io.ooze.daguerre.oss.domain.ChunkFileMetadata;
import io.ooze.daguerre.oss.domain.DeleteResult;
import io.ooze.daguerre.oss.domain.FileMetadata;
import io.ooze.daguerre.oss.domain.ObjectName;
import io.ooze.daguerre.utils.NewDateTimeUtils;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.*;
import io.ooze.daguerre.constant.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HuaweiCloudApi
 * @see <a href="https://support.huaweicloud.com/function-obs/index.html">对象存储服务 OBS</a>
 *
 * @author zhangjw
 * @date 2022/10/07 0007 17:15
 */
@Slf4j
public class HuaweiCloudApi extends AbstractOssApi {
    private static final String DEFAULT_REGION = "cn-north-4";

    private final OssProperties properties;
    private final ObsClient client;

    protected HuaweiCloudApi(OssProperties properties) {
        Assert.isTrue(vendor() == properties.getVendor(), "mismatched vendor");
        this.properties =properties;
        this.client = new ObsClient(properties.getAccessKey(),
                properties.getSecretKey(), properties.getEndpoint());
    }

    @Override
    public void createBucket(String bucket) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        try {
            CreateBucketRequest request = new CreateBucketRequest(bucket, properties.getRegion());
            request.setBucketStorageClass(StorageClassEnum.STANDARD);
            request.setAcl(AccessControlList.REST_CANNED_PRIVATE);
            client.createBucket(request);
            LifecycleConfiguration lifecycleConfiguration = new LifecycleConfiguration();
            ExpireMode.ALL.values().stream()
                    .filter(e -> -1 < e.expireDays())
                    .forEach(e -> lifecycleConfiguration.addRule(parseRule(lifecycleConfiguration, e)));
            client.setBucketLifecycle(new SetBucketLifecycleRequest(bucket, lifecycleConfiguration));
        } catch (ObsException e) {
            log.error("create bucket {} occurred exception", bucket, e);
            throw new OssException(vendor(), "create bucket failed", bucket, null, null, e);
        }
    }

    private LifecycleConfiguration.Rule parseRule(LifecycleConfiguration configuration, ExpireMode expireMode) {
        LifecycleConfiguration.Rule rule = configuration.new Rule(expireMode.name().toLowerCase() + "-Rule",
                expireMode.directory() + "/", true);
        rule.setExpiration(configuration.new Expiration(expireMode.expireDays()));
        return rule;
    }

    @Override
    public boolean bucketExists(String bucket) {
        Assert.hasText(bucket, "bucket name must not be null");
        try {
            return client.headBucket(bucket);
        } catch (ObsException e) {
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
            metadata.setUserMetadata(userMetadata(objectName).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            metadata.setContentType(MimeType.parse(objectName.original(), objectName.type()));
            if (-1 < objectName.expireDays()) {
                request.setExpires(objectName.expireDays());
                metadata.setExpires(GMT_DF.format(NewDateTimeUtils.localDateTime2Date(
                        NewDateTimeUtils.plusDay(LocalDateTime.now(), objectName.expireDays()))));
            }
            if (length > 0) {
                metadata.setContentLength(length);
            }
            request.setMetadata(metadata);
            client.putObject(request);
            return metadata(bucket, objectName.name());
        } catch (ObsException e) {
            log.error("upload file[bucket={}, absolutePath={}, objectName={}] failed:{}",
                    bucket, absolutePath, objectName.name(), e.getMessage());
            throw new OssException(vendor(), "upload file failed",
                    bucket, objectName.name(), absolutePath, e);
        } finally {
            log.info("upload file {}[{}] to {} cost {}ms.", objectName.original(),
                    objectName.name(), bucket, System.currentTimeMillis() - start);
        }
    }

    @Override
    public FileMetadata upload(String bucket, ObjectName objectName, InputStream stream,
                               long length) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.isTrue(Objects.nonNull(objectName), "objectName must not be null");
        Assert.notNull(stream, "file stream must not be null");
        createBucketIfMissing(bucket);
        long start = System.currentTimeMillis();
        try {
            PutObjectRequest request = new PutObjectRequest(bucket, objectName.name(), stream);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setUserMetadata(userMetadata(objectName).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            metadata.setContentType(MimeType.parse(objectName.original(), objectName.type()));
            if (-1 < objectName.expireDays()) {
                request.setExpires(objectName.expireDays());
                metadata.setExpires(GMT_DF.format(NewDateTimeUtils.localDateTime2Date(
                        NewDateTimeUtils.plusDay(LocalDateTime.now(), objectName.expireDays()))));
            }
            if (length > 0) {
                metadata.setContentLength(length);
            }
            request.setMetadata(metadata);
            client.putObject(request);
            return metadata(bucket, objectName.name());
        } catch (ObsException e) {
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
                request.setExpires(objectName.expireDays());
                metadata.setExpires(GMT_DF.format(NewDateTimeUtils.localDateTime2Date(
                        NewDateTimeUtils.plusDay(LocalDateTime.now(), objectName.expireDays()))));
            }
            metadata.setUserMetadata(userMetadata(objectName).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            request.setMetadata(metadata);
            return client.initiateMultipartUpload(request).getUploadId();
        } catch (ObsException e) {
            log.error("start multipart[bucket={}, objectName={}] upload failed:{}",
                    bucket, objectName.name(), e.getMessage());
            throw new OssException(vendor(), "gen file access url failed",
                    null, bucket, objectName.name(), e);
        }
    }

    @Override
    public ChunkFileMetadata chunkUpload(String bucket, ObjectName objectName, Path path,
                                         int index, long length) throws OssException {
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
            request.setInput(stream);
            request.setPartSize(length);
            request.setPartNumber(index+1);
            UploadPartResult result = client.uploadPart(request);
            return new ChunkFileMetadata(
                    new FileMetadata(objectName.id(), objectName.original(), bucket,
                            objectName.name()+"."+index, length, objectName.type(),
                            1, start, objectName.ossId()),
                    index,
                    Collections.singletonMap("etag", result.getEtag()));
        } catch (ObsException e) {
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
                    metadata.stream()
                            .map(e -> new PartEtag(e.getExtra().get("etag").toString(), e.getIndex()+1))
                            .collect(Collectors.toList()));
            client.completeMultipartUpload(request);
            return metadata(bucket, objectName.name());
        } catch (ObsException e) {
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
            TemporarySignatureRequest request = new TemporarySignatureRequest(
                    HttpMethodEnum.GET, bucket, objectName, null, expireMinutes*60L);
            return client.createTemporarySignature(request).getSignedUrl();
        } catch (ObsException e) {
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
        InputStream in;
        try {
            GetObjectRequest request = new GetObjectRequest(bucket, objectName);
            long rangeStart = (offset > -1) ? offset : 0;
            long rangeEnd = (length > -1) ? rangeStart+length-1 : -1;
            request.setRangeStart(offset);
            if (rangeEnd > -1) request.setRangeEnd(rangeEnd);
            return client.getObject(request).getObjectContent();
        } catch (ObsException e) {
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
        GetObjectRequest request = new GetObjectRequest(bucket, objectName);
        try (InputStream is = client.getObject(request).getObjectContent()) {
            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (ObsException | IOException e) {
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
            GetObjectMetadataRequest request = new GetObjectMetadataRequest(bucket, objectName);
            return transferMetadata(bucket, objectName, client.getObjectMetadata(request));
        } catch (ObsException | ParseException e) {
            log.error("get file[bucket={}, objectName={}] metadata failed:{}",
                    bucket, objectName, e.getMessage());
            throw new OssException(vendor(), "get file metadata failed",
                    bucket, objectName, null, e);
        }
    }

    private FileMetadata transferMetadata(String bucket, String objectName, ObjectMetadata source)
            throws ParseException {
        long expireDays = -1;
        if (StringUtils.hasText(source.getExpires())) {
            expireDays = (GMT_DF.parse(source.getExpires()).getTime() - source.getLastModified().getTime())
                    / DateTimeConstant.DAY_MILLS;
        }
        Map<String, Object> userMetadata = source.getAllMetadata();
        String filename = userMetadata.get("name").toString();
        return new FileMetadata(userMetadata.get("id").toString(), filename, bucket,
                objectName, source.getContentLength(), FileType.parse(filename),
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
        } catch (ObsException e) {
            log.error("remove file[bucket={}, objectName={}] failed:{}",
                    bucket, objectName, e.getMessage());
        }
        return false;
    }

    @Override
    public Collection<DeleteResult> delete(String bucket, Collection<String> objectNames)
            throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.notEmpty(objectNames, "object name set must not be empty");
        try {
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucket);
            for (String objectName : objectNames) {
                if (!StringUtils.hasText(objectName)) continue;
                request.addKeyAndVersion(objectName);
            }
            DeleteObjectsResult deleteResult = client.deleteObjects(request);
            List<DeleteResult> result = new LinkedList<>();
            deleteResult.getDeletedObjectResults().forEach(e ->
                    result.add(new DeleteResult(bucket, e.getObjectKey(), true)));
            deleteResult.getErrorResults().forEach(e ->
                    result.add(new DeleteResult(bucket, e.getObjectKey(), false)));
            return result;
        } catch (ObsException e) {
            log.error("remove file[bucket={}, objectName={}] failed:{}",
                    bucket, objectNames, e.getMessage());
        }
        return objectNames.stream().map(e -> new DeleteResult(bucket, e, false))
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(String bucket, String objectName) {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        try {
            return client.doesObjectExist(bucket, objectName);
        } catch (ObsException e) {
            log.error("check file[bucket={}, objectName={}] exists failed:{}",
                    bucket, objectName, e.getMessage());
        }
        return false;
    }

    @Override
    public Vendor vendor() {
        return Vendor.HUAWEICLOUD;
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            log.warn("close client failed:", e);
        }
    }
}
