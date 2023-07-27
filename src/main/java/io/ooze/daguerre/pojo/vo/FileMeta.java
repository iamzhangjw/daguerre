package io.ooze.daguerre.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ooze.daguerre.pojo.entity.File;
import lombok.Data;

/**
 * FileMeta
 *
 * @author zhangjw
 * @date 2022/10/26 0026 17:32
 */
@Data
public class FileMeta {
    private static FileMeta NULL = new FileMeta();

    /**
     * 文件标识
     */
    private String fileId;
    /**
     * 文件名称
     */
    private String filename;
    /**
     * 已上传字节长度
     */
    private Long uploadedLength;
    /**
     * 文件大小
     */
    private Long length;
    /**
     * 上传分片大小
     */
    private Integer chunkSize;
    /**
     * 文件类型
     */
    private String fileType;

    private long expiredAt;

    public static FileMeta from(File file) {
        if (null == file) return NULL;
        FileMeta meta = new FileMeta();
        meta.fileId = file.getUid();
        meta.length = file.getByteLength();
        meta.fileType = file.getType();
        meta.filename = file.getOriginalName();
        meta.uploadedLength = file.getByteLength();
        meta.chunkSize = file.getChunkSize();
        meta.expiredAt = file.getExpireAt();
        return meta;
    }

    public static FileMeta empty() {
        return NULL;
    }

    private FileMeta() {

    }

    @JsonIgnore
    public boolean isNull() {
        return this.equals(NULL);
    }
}
