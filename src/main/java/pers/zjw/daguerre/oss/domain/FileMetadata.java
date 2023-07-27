package pers.zjw.daguerre.oss.domain;

import pers.zjw.daguerre.constant.DateTimeConstant;
import pers.zjw.daguerre.constant.ExpireMode;
import pers.zjw.daguerre.constant.FileType;
import pers.zjw.daguerre.pojo.entity.File;
import pers.zjw.daguerre.pojo.entity.FileChunk;
import lombok.Getter;

/**
 * file metadata
 *
 * @author zhangjw
 * @date 2022/05/01 0001 11:28
 */
@Getter
public class FileMetadata {
    private final String id;
    private final String filename;
    private final String bucketName;
    private final String objectName;
    private final long length;
    private final long lastModified;
    private final FileType type;
    private String ossId;
    private final int expireDays;

    public FileMetadata(String id, String filename, String bucketName, String objectName,
                        long length, FileType type, int expireDays) {
        this(id, filename, bucketName, objectName, length, type, expireDays,
                System.currentTimeMillis(), null);
    }

    public FileMetadata(String id, String filename, String bucketName, String objectName,
                        long length, FileType type, int expireDays, long lastModified) {
        this(id, filename, bucketName, objectName, length, type, expireDays, lastModified, null);
    }

    public FileMetadata(String id, String filename, String bucketName, String objectName,
                        long length, FileType type, int expireDays, long lastModified, String ossId) {
        this.id = id;
        this.filename = filename;
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.length = length;
        this.type = type;
        this.expireDays = expireDays;
        this.ossId = ossId;
        this.lastModified = lastModified;
    }

    public FileMetadata(ObjectName objectName, String bucket, long length, long lastModified) {
        this(objectName.id(), objectName.original(), bucket, objectName.name(), length, objectName.type(),
                objectName.expireDays(), lastModified, objectName.ossId());
    }

    public static FileMetadata from(File file) {
        int expireAt = (-1 >= file.getExpireAt())
                ? -1 : (int) ((file.getExpireAt() - file.getCreateAt()) / DateTimeConstant.DAY_MILLS);
        return new FileMetadata(file.getUid(), file.getOriginalName(), file.getBucketName(),
                file.getStorePath(), file.getByteLength(), FileType.parse(file.getOriginalName()),
                expireAt, file.getVersion());
    }

    public static FileMetadata from(FileChunk chunk) {
        return new FileMetadata(chunk.getUid(), chunk.getOriginalName(), chunk.getBucketName(),
                chunk.getStorePath(), chunk.getByteLength(), FileType.parse(chunk.getOriginalName()),
                1, chunk.getVersion());
    }

    public File toFile() {
        File file = new File();
        file.setUid(id);
        file.setBucketName(bucketName);
        file.setOriginalName(filename);
        file.setStorePath(objectName);
        file.setType(type.name().toLowerCase());
        file.setByteLength(length);
        file.setCreateAt(lastModified);
        file.setVersion(System.currentTimeMillis());
        file.setOssId(ossId);
        if (-1 < expireDays) {
            file.setExpireAt(lastModified + (long) expireDays * DateTimeConstant.DAY_MILLS);
        } else {
            file.setExpireAt(-1L);
        }
        return file;
    }

    public ObjectName toObjectName() {
        ExpireMode mode = ExpireMode.parse(expireDays);
        if (ExpireMode.DEFAULT.equals(mode)) {
            return ObjectName.builder().with(id, objectName, filename, type, ossId, expireDays).build();
        }
        return ObjectName.builder().with(id, objectName, filename, type, ossId, mode).build();
    }

    public void setOssId(String ossId) {
        this.ossId = ossId;
    }
}
