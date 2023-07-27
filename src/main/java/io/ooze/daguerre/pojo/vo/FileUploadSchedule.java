package io.ooze.daguerre.pojo.vo;

import io.ooze.daguerre.pojo.entity.File;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 上传进度
 *
 * @author zhangjw
 * @date 2022/04/01 0001 18:02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FileUploadSchedule extends FileUpload {
    /**
     * 文件类型
     */
    private String fileType;
    /**
     * 已上传字节长度
     */
    private Long uploadedLength;

    private String md5sum;

    private FileUploadSchedule() {

    }

    public FileUploadSchedule(String fileType) {
        setFileType(fileType);
    }

    public static FileUploadSchedule from(File file) {
        FileUploadSchedule schedule = new FileUploadSchedule();
        schedule.setFileId(file.getUid());
        schedule.setFilename(file.getOriginalName());
        schedule.setLength(file.getByteLength());
        schedule.setFileType(file.getType());
        schedule.setChunkSize(file.getChunkSize());
        schedule.uploadedLength = file.getByteLength();
        return schedule;
    }

}
