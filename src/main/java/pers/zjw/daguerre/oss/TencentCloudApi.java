package pers.zjw.daguerre.oss;

import pers.zjw.daguerre.exception.OssException;
import pers.zjw.daguerre.oss.domain.ChunkFileMetadata;
import pers.zjw.daguerre.oss.domain.DeleteResult;
import pers.zjw.daguerre.oss.domain.FileMetadata;
import pers.zjw.daguerre.oss.domain.ObjectName;
import pers.zjw.daguerre.utils.NewDateTimeUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.lifecycle.LifecycleFilter;
import com.qcloud.cos.model.lifecycle.LifecyclePrefixPredicate;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import pers.zjw.daguerre.constant.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * TencentCloudApi
 * @see <a href="https://cloud.tencent.com/document/product/436">对象存储 COS</a>
 *
 * @author zhangjw
 * @date 2022/10/07 0007 17:16
 */
@Slf4j
public class TencentCloudApi extends AbstractOssApi {
    private static final String DEFAULT_REGION = "oss-cn-shenzhen";

    private final COSClient client;
    private final TransferManager transferManager;

    protected TencentCloudApi(OssProperties properties) {
        Assert.isTrue(vendor() == properties.getVendor(), "mismatched vendor");
        // 1 初始化用户身份信息（secretId, secretKey）。
        COSCredentials cred = new BasicCOSCredentials(properties.getAccessKey(), properties.getSecretKey());
        // 2 设置 bucket 的地域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region(properties.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        // 这里建议设置使用 https 协议
        // 从 5.6.54 版本开始，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        this.client = new COSClient(cred, clientConfig);

        ExecutorService threadPool = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2);
        this.transferManager = new TransferManager(this.client, threadPool);

        // 设置高级接口的配置项
        // 分块上传阈值和分块大小
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(OssApi.DEFAULT_CHUNK_SIZE);
        transferManagerConfiguration.setMinimumUploadPartSize(OssApi.DEFAULT_CHUNK_SIZE / 2);
        transferManager.setConfiguration(transferManagerConfiguration);
    }

    @Override
    public void createBucket(String bucket) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        try {
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucket);
            createBucketRequest.setCannedAcl(CannedAccessControlList.Private);
            client.createBucket(createBucketRequest);
            List<BucketLifecycleConfiguration.Rule> rules = ExpireMode.ALL.values().stream()
                    .filter(e -> -1 < e.expireDays())
                    .map(this::parseRule)
                    .collect(Collectors.toList());
            SetBucketLifecycleConfigurationRequest setBucketLifecycleConfigurationRequest =
                    new SetBucketLifecycleConfigurationRequest(bucket,
                            new BucketLifecycleConfiguration().withRules(rules));
            client.setBucketLifecycleConfiguration(setBucketLifecycleConfigurationRequest);
        } catch (CosClientException e) {
            log.error("create bucket {} occurred exception", bucket, e);
            throw new OssException(vendor(), "create bucket failed", bucket, null, null, e);
        }
    }

    private BucketLifecycleConfiguration.Rule parseRule(ExpireMode expireMode) {
        BucketLifecycleConfiguration.Rule rule = new BucketLifecycleConfiguration.Rule();
        rule.setId(expireMode.name().toLowerCase() + "-Rule");
        AbortIncompleteMultipartUpload abortIncompleteMultipartUpload = new AbortIncompleteMultipartUpload();
        abortIncompleteMultipartUpload.setDaysAfterInitiation(7);
        rule.setAbortIncompleteMultipartUpload(abortIncompleteMultipartUpload);
        rule.setFilter(new LifecycleFilter(new LifecyclePrefixPredicate(expireMode.directory() + "/")));
        rule.setExpirationInDays(expireMode.expireDays());
        rule.setStatus(BucketLifecycleConfiguration.ENABLED);
        return rule;
    }

    @Override
    public boolean bucketExists(String bucket) {
        Assert.hasText(bucket, "bucket name must not be null");
        try {
            return client.doesBucketExist(bucket);
        } catch (CosClientException e) {
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
            transferManager.upload(request).waitForUploadResult();
            return metadata(bucket, objectName.name());
        } catch (CosClientException | InterruptedException e) {
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
    public FileMetadata upload(String bucket, ObjectName objectName, InputStream stream, long length)
            throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.isTrue(Objects.nonNull(objectName), "objectName must not be null");
        Assert.notNull(stream, "file stream must not be null");
        createBucketIfMissing(bucket);
        long start = System.currentTimeMillis();
        try {
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
            PutObjectRequest request = new PutObjectRequest(bucket, objectName.name(), stream, metadata);
            client.putObject(request);
            transferManager.upload(request).waitForUploadResult();
            return metadata(bucket, objectName.name());
        } catch (CosClientException | InterruptedException e) {
            log.error("upload file[bucket={}, filename={}, objectName={}] failed:{}",
                    bucket, objectName.original(), objectName.name(), e.getMessage());
            throw new OssException(vendor(), "upload file stream failed",
                    bucket, objectName.name(), objectName.original(), e);
        } finally {
            log.info("upload file {}[{}] to {} cost {}ms.",
                    objectName.original(), objectName.name(), bucket, System.currentTimeMillis() - start);
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
        } catch (CosClientException e) {
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
            throw new OssException(vendor(), "upload file failed", bucket, objectName.name(), absolutePath, e);
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
            UploadPartRequest request = new UploadPartRequest();
            request.setBucketName(bucket);
            request.setKey(objectName.name());
            request.setUploadId(objectName.ossId());
            request.setInputStream(stream);
            request.setPartSize(length);
            request.setPartNumber(index+1);
            UploadPartResult result = client.uploadPart(request);
            return new ChunkFileMetadata(
                    new FileMetadata(objectName.id(), objectName.original(),
                            bucket, objectName.name()+"."+index,
                            length, objectName.type(), 1, start,
                            objectName.ossId()),
                    index,
                    Collections.singletonMap("etag", result.getETag()));
        } catch (CosClientException e) {
            log.error("upload chunk file[bucket={}, filename={}, objectName={}] failed:{}",
                    bucket, objectName.original(), objectName.name(), e.getMessage());
            throw new OssException(vendor(), "upload chunk file stream failed",
                    bucket, objectName.name(), objectName.original(), e);
        } finally {
            log.info("upload chunk file {}[{}] to {} cost {}ms.",
                    objectName.original(), objectName.name(), bucket, System.currentTimeMillis() - start);
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
                            .map(e -> new PartETag(e.getIndex()+1, e.getExtra().get("etag").toString()))
                            .collect(Collectors.toList()));
            client.completeMultipartUpload(request);
            return metadata(bucket, objectName.name());
        } catch (CosClientException e) {
            log.error("compose file[bucket={}, objectName={}] with {} failed:{}",
                    bucket, objectName.name(), metadata, e.getMessage());
            throw new OssException(vendor(), "splice file failed",
                    bucket, objectName.name(), objectName.original(), e);
        } finally {
            log.info("compose file {} in {} with {} cost {}ms.",
                    objectName.name(), bucket, metadata, System.currentTimeMillis() - start);
        }
    }

    @Override
    public String url(String bucket, String objectName, int expireMinutes) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        try {
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectName, HttpMethodName.GET);
            request.setExpiration(NewDateTimeUtils.localDateTime2Date(LocalDateTime.now().plusMinutes(expireMinutes)));
            return client.generatePresignedUrl(request).toString();
        } catch (CosClientException e) {
            log.error("gen file[bucket={}, objectName={}] access url failed:{}", bucket, objectName, e.getMessage());
            throw new OssException(vendor(), "gen file access url failed",
                    null, bucket, objectName, e);
        }
    }

    @Override
    public InputStream download(String bucket, String objectName, long offset, long length) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        long start = System.currentTimeMillis();
        try {
            GetObjectRequest request = new GetObjectRequest(bucket, objectName);
            long rangeStart = (offset > -1) ? offset : 0;
            long rangeEnd = (length > -1) ? rangeStart+length-1 : -1;
            request.setRange(rangeStart, rangeEnd);
            return client.getObject(request).getObjectContent();
        } catch (CosClientException e) {
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
            transferManager.download(new GetObjectRequest(bucket, objectName), path.toFile()).waitForCompletion();
        } catch (CosClientException | InterruptedException e) {
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
            return transferMetadata(bucket, objectName, client.getObjectMetadata(bucket, objectName));
        } catch (CosClientException e) {
            log.error("get file[bucket={}, objectName={}] metadata failed:{}",
                    bucket, objectName, e.getMessage());
            throw new OssException(vendor(), "get file metadata failed",
                    bucket, objectName, null, e);
        }
    }

    private static FileMetadata transferMetadata(String bucket, String objectName, ObjectMetadata source) {
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
        } catch (CosClientException e) {
            log.error("remove file[bucket={}, objectName={}] failed:{}",
                    bucket, objectName, e.getMessage());
        }
        return false;
    }

    @Override
    public Collection<DeleteResult> delete(String bucket, Collection<String> objectNames) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.notEmpty(objectNames, "object name set must not be empty");
        try {
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucket);
            List<DeleteObjectsRequest.KeyVersion> keys = new LinkedList<>();
            for (String objectName : objectNames) {
                if (!StringUtils.hasText(objectName)) continue;
                keys.add(new DeleteObjectsRequest.KeyVersion(objectName));
            }
            request.setKeys(keys);
            DeleteObjectsResult deleteResult = client.deleteObjects(request);
            return deleteResult.getDeletedObjects().stream()
                    .map(e -> new DeleteResult(bucket, e.getKey(), true))
                    .collect(Collectors.toList());
        } catch (MultiObjectDeleteException e) {
            List<DeleteResult> result = new LinkedList<>();
            e.getDeletedObjects().forEach(a -> result.add(new DeleteResult(bucket, a.getKey(), true)));
            e.getErrors().forEach(a -> result.add(new DeleteResult(bucket, a.getKey(), false)));
            return result;
        } catch (CosClientException e) {
            log.error("remove file[bucket={}, objectName={}] failed:{}",
                    bucket, objectNames, e.getMessage());
        }
        return objectNames.stream().map(e -> new DeleteResult(bucket, e, false)).collect(Collectors.toList());
    }

    @Override
    public boolean exists(String bucket, String objectName) {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        try {
            return client.doesObjectExist(bucket, objectName);
        } catch (CosClientException e) {
            log.error("check file[bucket={}, objectName={}] exists failed:{}",
                    bucket, objectName, e.getMessage());
        }
        return false;
    }

    @Override
    public Vendor vendor() {
        return Vendor.TENCENTCLOUD;
    }

    @Override
    public void close() {
        transferManager.shutdownNow(true);
    }
}
