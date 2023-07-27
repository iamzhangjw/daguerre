package io.ooze.daguerre.pojo.vo;

import lombok.Data;

import java.util.Objects;

/**
 * access file
 *
 * @author zhangjw
 * @date 2022/4/3 0003 9:02
 */
@Data
public class AccessFile {
    /**
     * 文件id
     */
    private String id;

    /**
     * 存储路径
     */
    private String storePath;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 桶名称
     */
    private String bucketName;

    /**
     * 文件大小
     */
    private Long byteLength;
    /**
     * 文件访问url
     */
    private String accessUrl;
    /**
     * 到期时间戳
     */
    private Long expireAt;

    public boolean available() {
        return Objects.nonNull(expireAt) && expireAt > System.currentTimeMillis();
    }
}
