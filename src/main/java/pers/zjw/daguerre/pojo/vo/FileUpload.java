package pers.zjw.daguerre.pojo.vo;

import lombok.Data;

/**
 * FileUpload
 *
 * @author zhangjw
 * @date 2022/11/07 0007 10:16
 */
@Data
public class FileUpload {
    /**
     * 文件标识
     */
    private String fileId;
    /**
     * 文件名称
     */
    private String filename;
    /**
     * 文件大小
     */
    private Long length;
    /**
     * 上传分片大小
     */
    private Integer chunkSize;
    /**
     * 分片索引，从 0 开始
     */
    private Integer index;
    /**
     * 保留天数，-1 表示永久
     */
    private Integer expireDays = -1;
}
