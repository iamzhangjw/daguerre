package io.ooze.daguerre.oss;

import io.ooze.daguerre.exception.OssException;
import io.ooze.daguerre.oss.domain.ChunkFileMetadata;
import io.ooze.daguerre.oss.domain.DeleteResult;
import io.ooze.daguerre.oss.domain.FileMetadata;
import io.ooze.daguerre.oss.domain.ObjectName;
import io.ooze.daguerre.utils.HttpActuator;
import io.ooze.daguerre.utils.NewDateTimeUtils;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.*;
import io.ooze.daguerre.constant.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * minio api
 * @see <a href="https://docs.min.io">MinIO Quickstart Guide</a>
 * <a href="http://docs.minio.org.cn/docs">MinIO Quickstart Guide| Minio中文文档</a>
 *
 * @author zhangjw
 * @date 2022/03/30 0030 16:31
 */
@Slf4j
public class MinioApi extends AbstractOssApi {
    private static final String DEFAULT_REGION = "cn-1";

    private final MinioClient client;

    protected MinioApi(OssProperties properties) {
        Assert.isTrue(vendor() == properties.getVendor(), "mismatched vendor");
        this.client = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .region(StringUtils.hasText(properties.getRegion()) ? properties.getRegion() : DEFAULT_REGION)
                .httpClient(HttpActuator.INSTANCE.client())
                .build();
    }

    @Override
    public void createBucket(String bucket) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        try {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket)/*.objectLock(true)*/.build());
            List<LifecycleRule> rules = ExpireMode.ALL.values().stream()
                    .filter(e -> -1 < e.expireDays())
                    .map(this::parseRule)
                    .collect(Collectors.toList());
            client.setBucketLifecycle(SetBucketLifecycleArgs.builder()
                    .bucket(bucket).config(new LifecycleConfiguration(rules)).build());
            /*ObjectLockConfiguration lockConfiguration =
                    new ObjectLockConfiguration(RetentionMode.COMPLIANCE, new RetentionDurationDays(1));
            client.setObjectLockConfiguration(SetObjectLockConfigurationArgs.builder()
                    .bucket(bucket).config(lockConfiguration).build());*/
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("create bucket {} occurred exception", bucket, e);
            throw new OssException(vendor(), "create bucket failed", bucket, null, null, e);
        }
    }

    private LifecycleRule parseRule(ExpireMode expireMode) {
        return new LifecycleRule(Status.ENABLED,
                null,
                new Expiration((ZonedDateTime) null, expireMode.expireDays(), null),
                new RuleFilter(expireMode.directory() + "/"),
                expireMode.name().toLowerCase() + "-Rule",
                null,
                null,
                null);
    }

    @Override
    public boolean bucketExists(String bucket) {
        Assert.hasText(bucket, "bucket name must not be null");
        try {
            return client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException e) {
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
        Assert.isTrue(length >= -1, "file length must greater or equal than -1");
        createBucketIfMissing(bucket);
        long start = System.currentTimeMillis();
        String absolutePath = path.toAbsolutePath().toString();
        try {
            UploadObjectArgs.Builder builder = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName.name())
                    .filename(absolutePath)
                    .contentType(MimeType.parse(objectName.original(), objectName.type()))
                    .userMetadata(userMetadata(objectName));
            client.uploadObject(builder.build());
            return metadata(bucket, objectName.name());
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException e) {
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
        Assert.isTrue(length >= -1, "file length must greater or equal than -1");
        createBucketIfMissing(bucket);
        long start = System.currentTimeMillis();
        try {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName.name())
                    .stream(stream, length, DEFAULT_CHUNK_SIZE)
                    .contentType(MimeType.parse(objectName.original(), objectName.type()))
                    .userMetadata(userMetadata(objectName));
            client.putObject(builder.build());
            return metadata(bucket, objectName.name());
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException e) {
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
    public ChunkFileMetadata chunkUpload(String bucket, ObjectName objectName, Path path, int index, long length)
            throws OssException {
        Assert.isTrue(index > -1, "index must not be negative");
        Assert.isTrue(length > 0, "chunk file length must greater than 0");
        return new ChunkFileMetadata(upload(bucket, objectName.chunk(index), path, length), index);
    }

    @Override
    public ChunkFileMetadata chunkUpload(String bucket, ObjectName objectName,
                                         InputStream stream, int index, long length) throws OssException {
        Assert.isTrue(index > -1, "index must not be negative");
        Assert.isTrue(length > 0, "chunk file length must greater than 0");
        return new ChunkFileMetadata(upload(bucket, objectName.chunk(index), stream, length), index);
    }

    @Override
    public FileMetadata completeMultiChunkUpload(String bucket, ObjectName objectName,
                                                 List<ChunkFileMetadata> metadata) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.isTrue(Objects.nonNull(objectName), "objectName must not be null");
        Assert.notEmpty(metadata, "chunk object metadata must not be null");
        long start = System.currentTimeMillis();
        List<ComposeSource> sourceObjectList = metadata.stream()
                .sorted(Comparator.comparingInt(ChunkFileMetadata::getIndex))
                .map(e -> ComposeSource.builder().bucket(e.getBucketName()).object(e.getObjectName()).build())
                .collect(Collectors.toList());
        try {
            ComposeObjectArgs.Builder builder = ComposeObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName.name())
                    .sources(sourceObjectList)
                    .headers(Collections.singletonMap("Content-Type",
                            MimeType.parse(objectName.original(), objectName.type())))
                    .userMetadata(userMetadata(objectName));
            client.composeObject(builder.build());
            metadata.stream().collect(Collectors.groupingBy(FileMetadata::getBucketName))
                    .forEach((k, v) ->
                            delete(k, v.stream().map(ChunkFileMetadata::getObjectName)
                                    .collect(Collectors.toList())));
            return metadata(bucket, objectName.name());
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("compose file[bucket={}, objectName={}] with {} failed:{}",
                    bucket, objectName.name(), metadata, e.getMessage());
            throw new OssException(vendor(), "compose file failed",
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
            return client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(expireMinutes * 60)
                            .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException e) {
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
        GetObjectArgs.Builder builder = GetObjectArgs.builder().bucket(bucket).object(objectName);
        if (offset > -1) builder.offset(offset);
        if (length > -1) builder.length(length);
        try {
            return client.getObject(builder.build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException e) {
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
            client.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .filename(absolutePath)
                            .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException e) {
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
            return transferMetadata(client.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()));
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException
                | ParseException e) {
            log.error("get file[bucket={}, objectName={}] metadata failed:{}",
                    bucket, objectName, e.getMessage());
            throw new OssException(vendor(), "get file metadata failed",
                    bucket, objectName, null, e);
        }
    }

    private static FileMetadata transferMetadata(StatObjectResponse source) throws ParseException {
        Map<String, String> userMetadata = source.userMetadata();
        long expireDays = -1;
        if (null != userMetadata.get("Expires")) {
            expireDays = (GMT_DF.parse(userMetadata.get("Expires")).getTime()/1000
                    - source.lastModified().toEpochSecond()) / DateTimeConstant.DAY_SECOND;
        }
        return new FileMetadata(userMetadata.get("id"), userMetadata.get("name"), source.bucket(),
                source.object(), source.size(), FileType.parse(userMetadata.get("name")),
                (int) expireDays, source.lastModified().toEpochSecond() * 1000);
    }

    @Override
    public boolean delete(String bucket, String objectName) throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.hasText(objectName, "object name must not be null");
        try {
            client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
            log.info("remove file {} in {}.", objectName, bucket);
            return true;
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("remove oss file[bucket={}, objectName={}] failed:{}",
                    bucket, objectName, e.getMessage());
        }
        return false;
    }

    @Override
    public Collection<io.ooze.daguerre.oss.domain.DeleteResult> delete(String bucket, Collection<String> objectNames)
            throws OssException {
        Assert.hasText(bucket, "bucket name must not be null");
        Assert.notEmpty(objectNames, "object name set must not be empty");
        Iterable<Result<DeleteError>> results = client.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket)
                        .objects(objectNames.stream().filter(StringUtils::hasText)
                                .map(DeleteObject::new).collect(Collectors.toList()))
                        .build());
        Map<String, io.ooze.daguerre.oss.domain.DeleteResult> map = new HashMap<>(objectNames.size());
        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                log.info("remove chunk file {} in {} error:{}",
                        error.objectName(), error.bucketName(), error.message());
                map.put(error.bucketName()+error.objectName(),
                        new io.ooze.daguerre.oss.domain.DeleteResult(error.bucketName(), error.objectName(), true));
            } catch (ErrorResponseException | InsufficientDataException | InternalException
                    | InvalidKeyException | InvalidResponseException | IOException
                    | NoSuchAlgorithmException | ServerException | XmlParserException e) {
                log.error("remove oss file failed:", e);
            }
        }
        objectNames.forEach(e -> map.computeIfAbsent(bucket+e, key ->
                new DeleteResult(bucket, e, false)));
        return map.values();
    }

    @Override
    public boolean exists(String bucket, String objectName) {
        try {
            metadata(bucket, objectName);
        } catch (OssException e) {
            return false;
        }
        return true;
    }

    @Override
    public Vendor vendor() {
        return Vendor.MINIO;
    }

    @Override
    protected Map<String, String> userMetadata(ObjectName objectName) {
        Map<String, String> map = super.userMetadata(objectName);
        if (-1 < objectName.expireDays()) {
            map.put("Expires", GMT_DF.format(NewDateTimeUtils.localDateTime2Date(
                    NewDateTimeUtils.plusDay(LocalDateTime.now(), objectName.expireDays()))));
        }
        return map;
    }
}
