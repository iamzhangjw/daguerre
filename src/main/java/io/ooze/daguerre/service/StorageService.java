package io.ooze.daguerre.service;

import io.ooze.daguerre.constant.ExpireMode;
import io.ooze.daguerre.constant.FileType;
import io.ooze.daguerre.constant.ThumbnailType;
import io.ooze.daguerre.exception.BizException;
import io.ooze.daguerre.exception.ErrorCode;
import io.ooze.daguerre.oss.OssApi;
import io.ooze.daguerre.oss.OssHolder;
import io.ooze.daguerre.oss.domain.*;
import io.ooze.daguerre.pojo.entity.File;
import io.ooze.daguerre.pojo.entity.FileChunk;
import io.ooze.daguerre.pojo.vo.*;
import io.ooze.daguerre.utils.ThumbnailTailor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * storage service
 *
 * @author zhangjw
 * @date 2022/4/3 0003 9:09
 */
@Slf4j
@Service
public class StorageService {
    private ObjectNameFactory objectNameFactory;
    @Autowired
    private OssService ossService;
    @Autowired
    private FileChunkService chunkService;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileUrlService urlService;

    @Value("${daguerre.url.expireMinutes:120}")
    private int defaultExpireMinutes;

    @PostConstruct
    private void setup() {
        objectNameFactory = new ObjectNameFactory("uuid");
    }

    /**
     * buffer size is 5MB
     */
    private static final int BUFFER_SIZE = OssApi.DEFAULT_CHUNK_SIZE / 2;

    /**
     * 7天
     */
    private static final int URL_MAX_EXPIRE_MINUTES = 10_080;

    public FileMeta meta(String uid) {
        File file = fileService.getByFileId(uid, accessKey());
        if (Objects.isNull(file)) return FileMeta.empty();
        return FileMeta.from(file);
    }

    //@Transactional(propagation= Propagation.REQUIRED, rollbackFor = Exception.class)
    public FileUploadSchedule upload(MultipartFile file, FileUpload upload) {
        Assert.isTrue(Objects.nonNull(file) && !file.isEmpty(), "file must not be null");
        Assert.notNull(upload, "upload schedule must not be null");
        Assert.isTrue(null == upload.getChunkSize()
                        || upload.getChunkSize() <= OssApi.MAX_CHUNK_SIZE,
                "chunk size over limit, max size " + OssApi.MAX_CHUNK_SIZE);
        /*
         * 超过一定长度的文件会触发分片上传
         *
         * 1.指定文件id，意味着文件已上传过一部分；
         * 2.文件大小超过程序设置的最大分片大小；
         * 3.没有指定分片大小，文件大小超过程序设置的默认分片大小；
         * 4.参数中文件大小超过程序设置的最大分片大小；
         * 5.没有指定分片大小，参数中文件大小超过程序设置的默认分片大小；
         * 6.文件大小超过指定分片大小；
         * 7.分片大小超过参数中文件大小；
         */
        FileUploadSchedule schedule;
        if (Objects.nonNull(upload.getFileId())
                || file.getSize() > OssApi.MAX_CHUNK_SIZE
                || (Objects.isNull(upload.getChunkSize()) && file.getSize() > OssApi.DEFAULT_CHUNK_SIZE)
                || (Objects.nonNull(upload.getLength()) && upload.getLength() > OssApi.MAX_CHUNK_SIZE)
                || (Objects.isNull(upload.getChunkSize())
                && Objects.nonNull(upload.getLength()) && upload.getLength() > OssApi.DEFAULT_CHUNK_SIZE)
                || (Objects.nonNull(upload.getChunkSize()) && file.getSize() > upload.getChunkSize())
                || (Objects.nonNull(upload.getChunkSize())
                && Objects.nonNull(upload.getLength()) && upload.getLength() > upload.getChunkSize())) {
            schedule = FileUploadSchedule.from(chunkUpload(file, upload));
        } else {
            schedule = FileUploadSchedule.from(singleUpload(file, upload));
        }
        schedule.setIndex(upload.getIndex());
        schedule.setExpireDays(upload.getExpireDays());
        return schedule;
    }

    public String url(String uid, Integer expireMinutes) {
        int validExpireMinutes = Math.min((Objects.isNull(expireMinutes) || expireMinutes <= 0)
                ? defaultExpireMinutes : expireMinutes,
                URL_MAX_EXPIRE_MINUTES);
        AccessFile accessFile = fileService.getFileIncludeUrl(uid, accessKey());
        if (Objects.isNull(accessFile)) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (StringUtils.hasText(accessFile.getAccessUrl()) && accessFile.available()) {
            return accessFile.getAccessUrl();
        }
        long expireAt = validExpireMinutes * 60_000L + System.currentTimeMillis();
        String url = ossService.url(accessFile.getBucketName(), accessFile.getStorePath(),
                validExpireMinutes);
        if (StringUtils.hasText(accessFile.getAccessUrl())) {
            urlService.update(uid, url, expireAt);
        } else {
            urlService.insert(uid, url, expireAt);
        }
        return url;
    }

    public Collection<FileUrlVO> url(FileUrlQuery query) {
        if (StringUtils.hasText(query.getAttach())) {
            Optional<ThumbnailType> thumbnailType = ThumbnailType.parse(query.getAttach());
            if (!thumbnailType.isPresent()) {
                throw new BizException(ErrorCode.MISMATCH_PARAM);
            }
        }
        int expireMinutes = Math.min((Objects.isNull(query.getExpireMinutes()) || query.getExpireMinutes() <= 0)
                ? defaultExpireMinutes : query.getExpireMinutes(),
                URL_MAX_EXPIRE_MINUTES);
        Collection<FileUrlVO> fileUrls = new LinkedList<>(url(query.getFileIds(),
                query.getAttach(), expireMinutes));
        if (StringUtils.hasText(query.getAttach()) && fileUrls.size() < query.getFileIds().size()) {
            Collection<String> notFoundIds = new LinkedList<>(query.getFileIds());
            for (FileUrlVO fileUrl : fileUrls) {
                notFoundIds.remove(fileUrl.getFileId());
            }
            fileUrls.addAll(url(notFoundIds, null, expireMinutes));
        }
        return fileUrls;
    }

    private Collection<FileUrlVO> url(Collection<String> uids, String attach, Integer expireMinutes) {
        Collection<AccessFile> accessFiles = fileService.getFileIncludeUrl(uids, accessKey(), attach);
        Collection<FileUrlVO> fileUrls = new LinkedList<>();
        long expireAt = expireMinutes * 60_000L + System.currentTimeMillis();
        for (AccessFile accessFile : accessFiles) {
            if (StringUtils.hasText(accessFile.getAccessUrl()) && accessFile.available()) {
                fileUrls.add(new FileUrlVO(accessFile.getId(), accessFile.getAccessUrl()));
                continue;
            }
            String url = ossService.url(accessFile.getBucketName(),
                    accessFile.getStorePath(), expireMinutes);
            if (StringUtils.hasText(accessFile.getAccessUrl())) {
                urlService.update(accessFile.getId(), attach, url, expireAt);
            } else {
                urlService.insert(accessFile.getId(), attach, url, expireAt);
            }
            fileUrls.add(new FileUrlVO(accessFile.getId(), url));
        }
        return fileUrls;
    }

    public ResponseEntity<Resource> download(String uid, Long offset, Long length) {
        File file = fileService.getByFileId(uid, accessKey());
        if (Objects.isNull(file)) {
            return ResponseEntity.notFound().build();
        }
        if (-1 < file.getExpireAt() && file.getExpireAt() <= System.currentTimeMillis()) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        long actualOffset = (Objects.nonNull(offset) && offset > -1) ? offset : -1;
        long actualLength = (Objects.nonNull(length) && length > 0) ? length : -1;

        InputStream stream = ossService.download(file.getBucketName(), file.getStorePath(),
                actualOffset, actualLength);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getOriginalName() + "\"")
                .contentLength(actualLength > -1 ? actualLength : file.getByteLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    protected File singleUpload(MultipartFile file, FileUpload upload) {
        String filename = ( StringUtils.hasText(upload.getFilename()))
                ? upload.getFilename() : file.getOriginalFilename();
        FileType fileType = FileType.parse(filename);
        return singleUpload(file, assembleObjectName(filename, fileType, upload.getExpireDays()));
    }

    private ObjectName assembleObjectName(String filename, FileType fileType, Integer expireDays) {
        int actualExpireDays = (null == expireDays || -1 >= expireDays) ? -1 : expireDays;
        ExpireMode expireMode = ExpireMode.parse(actualExpireDays);
        ObjectName objectName;
        if (ExpireMode.DEFAULT == expireMode) {
            objectName = objectNameFactory.create(filename, fileType, actualExpireDays);
        } else {
            objectName = objectNameFactory.create(filename, fileType, expireMode);
        }
        return objectName;
    }

    private ObjectName assembleObjectName(FileUpload upload, File file) {
        int expireDays = (null == upload.getExpireDays() || -1 >= upload.getExpireDays())
                ? -1 : upload.getExpireDays();
        ExpireMode expireMode = ExpireMode.parse(expireDays);
        ObjectName objectNameObj;
        if (ExpireMode.DEFAULT == expireMode) {
            objectNameObj = objectNameFactory.create(upload.getFileId(), file.getStorePath(),
                    file.getOriginalName(), FileType.parse(file.getOriginalName()), file.getOssId(),
                    expireDays);
        } else {
            objectNameObj = objectNameFactory.create(upload.getFileId(), file.getStorePath(),
                    file.getOriginalName(), FileType.parse(file.getOriginalName()), file.getOssId(),
                    expireMode);
        }
        return objectNameObj;
    }

    protected File singleUpload(MultipartFile file, ObjectName objectName) {
        FileMetadata fileMetadata = new FileMetadata(objectName, bucketName(),
                file.getSize(), System.currentTimeMillis());
        try (InputStream is = file.getInputStream()) {
            return singleUpload(is, fileMetadata);
        } catch (IOException e) {
            throw new BizException(ErrorCode.READ_STREAM_FAILED, e);
        }
    }

    protected File singleUpload(InputStream inputStream, FileMetadata fileMetadata) {
        long start = System.currentTimeMillis();
        try {
            if (FileType.IMG == fileMetadata.getType()) {
                return uploadImage(inputStream, fileMetadata);
            }
            FileMetadata uploadFileMetadata = ossService.upload(fileMetadata, inputStream);
            return fileService.insertWholeFile(uploadFileMetadata, accessKey());
        } catch (IOException e) {
            throw new BizException(ErrorCode.READ_STREAM_FAILED, e);
        } finally {
            log.info("upload file {} cost {}ms", fileMetadata, System.currentTimeMillis() - start);
        }
    }

    private File uploadImage(InputStream inputStream, FileMetadata metadata) throws IOException {
        java.io.File tmpFile = createTempFile();
        try (OutputStream os = Files.newOutputStream(tmpFile.toPath())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read = 0;
            while((read = inputStream.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
        }

        if (ThumbnailTailor.needResize(tmpFile.length())) {
            ThumbImageObjectName thumbImageObjectName = new ThumbImageObjectName(metadata, ThumbnailType.THUMBNAIL);
            try (InputStream is = ThumbnailTailor.imageThumbnail(tmpFile, metadata.getFilename())) {
                uploadThumbnail(is, new FileMetadata(thumbImageObjectName, metadata.getBucketName(),
                                is.available(), System.currentTimeMillis()),
                        thumbImageObjectName.thumbnailType());
            } catch (IOException e) {
                log.warn("resize image thumbnail failed: {}", metadata.getFilename(), e);
            }
        }
        FileMetadata uploadFileMetadata = ossService.upload(metadata, tmpFile);
        tmpFile.delete();
        return fileService.insertWholeFile(uploadFileMetadata, accessKey());
    }

    protected File uploadThumbnail(InputStream inputStream, FileMetadata fileMetadata,
                                ThumbnailType thumbnailType) {
        long start = System.currentTimeMillis();
        FileMetadata uploadFileMetadata = ossService.upload(fileMetadata, inputStream);
        File thumbnailFile = fileService.insertWholeFile(uploadFileMetadata, accessKey(), thumbnailType);
        log.info("upload thumbnail file {} cost {}ms", fileMetadata, System.currentTimeMillis() - start);
        return thumbnailFile;
    }

    private File chunkUpload(MultipartFile file, FileUpload upload) {
        long start = System.currentTimeMillis();
        if (Objects.isNull(upload.getFileId())) {
            String filename = StringUtils.hasText(upload.getFilename())
                    ? upload.getFilename() : file.getOriginalFilename();
            FileType fileType = FileType.parse(filename);
            long length = Objects.nonNull(upload.getLength()) ? upload.getLength() : file.getSize();
            ObjectName objectName = assembleObjectName(filename, fileType, upload.getExpireDays());
            FileMetadata fileMetadata = new FileMetadata(objectName, bucketName(), length, start);
            try (InputStream stream = file.getInputStream()) {
                return uploadFromFirstChunk(stream, file.getSize(), fileMetadata,
                        upload.getChunkSize());
            } catch (IOException e) {
                throw new BizException(ErrorCode.READ_STREAM_FAILED, e);
            } finally {
                log.info("upload file {} cost {}ms", fileMetadata, System.currentTimeMillis() - start);
            }
        }
        File dbFile = fileService.getByFileId(upload.getFileId(), accessKey());
        if (Objects.isNull(dbFile)) throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        if (dbFile.getCompleted()) {
            log.debug("file {} already upload complete.", dbFile.getUid());
            return dbFile;
        }
        ObjectName objectName = assembleObjectName(upload, dbFile);
        FileMetadata fileMetadata = new FileMetadata(objectName, dbFile.getBucketName(),
                dbFile.getByteLength(), dbFile.getVersion());
        if (file.getSize() > dbFile.getByteLength()) {
            throw new BizException(ErrorCode.FILE_SIZE_MISMATCH);
        }
        if (Objects.nonNull(upload.getIndex()) && upload.getIndex() >= dbFile.getChunkCount()) {
            throw new BizException(ErrorCode.MISMATCH_PARAM, "illegal chunk index");
        }
        try (InputStream is = file.getInputStream()) {
            if (null == upload.getIndex() || upload.getIndex() < 0) {
                return uploadLeftChunk(is, file.getSize(), fileMetadata, dbFile);
            }
            return chunkUploadWithIndex(is, file.getSize(), fileMetadata, dbFile, upload.getIndex());
        } catch (IOException e) {
            throw new BizException(ErrorCode.READ_STREAM_FAILED, e);
        } finally {
            log.info("upload file {} cost {}ms", fileMetadata, System.currentTimeMillis() - start);
        }
    }

    private File uploadFromFirstChunk(InputStream inputStream, long length,
                                      FileMetadata fileMetadata, Integer chunkSize)
            throws IOException {
        int actualChunkSize = Math.min((null == chunkSize) ? OssApi.DEFAULT_CHUNK_SIZE : chunkSize,
                OssApi.MAX_CHUNK_SIZE);
        fileMetadata.setOssId(ossService.startMultipartUpload(fileMetadata));
        File majorFile = fileService.insertFileMetadata(fileMetadata, accessKey(), actualChunkSize);
        return uploadLeftChunk(inputStream, length, fileMetadata, majorFile);
    }

    private File uploadLeftChunk(InputStream inputStream, long length, FileMetadata fileMetadata, File file)
            throws IOException {
        int uploadLength = 0;
        if (length == file.getByteLength() && file.getUploadedLength() > 0
                && file.getUploadedLength() < file.getByteLength()) {
            inputStream.skip(file.getUploadedLength());
            uploadLength += file.getUploadedLength();
        }
        if (file.getUploadedLength() < file.getByteLength()) {
            int chunkSize = file.getChunkSize();
            int index = file.getUploadedChunk();
            do {
                uploadLength += chunkUpload(inputStream, fileMetadata, chunkSize, index);
                index++;
            } while (length > uploadLength);
        }
        if ((file.getUploadedLength()+uploadLength) >= fileMetadata.getLength()) {
            composeMultiChunks(fileMetadata, file.getUid());
        }
        return fileService.getByFileId(file.getUid(), accessKey());
    }

    private File chunkUploadWithIndex(InputStream stream, long length,
                                      FileMetadata fileMetadata, File file, int index)
            throws IOException {
        if (!file.getCompleted()) {
            if (length == file.getByteLength() && index > 0) {
                stream.skip((long)file.getChunkSize()*index);
            }
            chunkUpload(stream, fileMetadata, file.getChunkSize(), index);
            composeMultiChunks(fileMetadata, file.getUid());
        }
        return fileService.getByFileId(file.getUid(), accessKey());
    }

    private void composeMultiChunks(FileMetadata fileMetadata, String uid) {
        File file = fileService.getByFileId(uid, accessKey());
        if (file.getCompleted()) return;
        List<FileChunk> chunkList = chunkService.listByFileId(file.getUid());
        if (CollectionUtils.isEmpty(chunkList) || chunkList.size() != file.getChunkCount()) return;
        ossService.completeMultiChunkUpload(fileMetadata.getBucketName(), fileMetadata.toObjectName(),
                chunkList.stream().map(ChunkFileMetadata::new).collect(Collectors.toList()));
        fileService.updateCompletedFile(file.getUid(), accessKey(), chunkList.get(0).getVersion());
    }

    private int chunkUpload(InputStream inputStream, FileMetadata fileMetadata, int chunkSize, int index)
            throws IOException {
        if (chunkService.exists(fileMetadata.getId(), index)) return 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bufferCount = (int)Math.ceil((double) chunkSize / BUFFER_SIZE);
        java.io.File tmpFile = createTempFile();
        try (OutputStream os = Files.newOutputStream(tmpFile.toPath())) {
            int read = 0, readTimes = 0;
            while(readTimes < bufferCount && (read = inputStream.read(buffer)) > 0) {
                os.write(buffer, 0, read);
                readTimes++;
            }
        }
        if (0 == index && FileType.PLAY == fileMetadata.getType()) {
            // 是否需要异步处理
            uploadVideoCover(tmpFile, fileMetadata);
        }
        ChunkFileMetadata chunkFileMetadata = ossService.chunkUpload(fileMetadata, tmpFile, index);
        tmpFile.delete();
        // insert chunk record and update upload schedule
        fileService.insertFileChunkAndUpdateFileMetadata(chunkFileMetadata);
        return (int)chunkFileMetadata.getLength();
    }

    private void uploadVideoCover(java.io.File file, FileMetadata metadata) {
        ThumbImageObjectName thumbImageObjectName = new ThumbImageObjectName(metadata, ThumbnailType.COVER);
        try (InputStream is = ThumbnailTailor.videoCover(file, metadata.getFilename())) {
            uploadThumbnail(is, new FileMetadata(thumbImageObjectName, metadata.getBucketName(),
                            is.available(), System.currentTimeMillis()),
                    thumbImageObjectName.thumbnailType());
        } catch (IOException e) {
            log.warn("grab video cover failed: {}", metadata.getFilename(), e);
        }
    }

    private java.io.File createTempFile() throws IOException {
        return Files.createTempFile("dag-", ".tmp").toFile();
    }

    private String bucketName() {
        return OssHolder.bucket();
    }

    private String accessKey() {
        return OssHolder.accessKey();
    }
}