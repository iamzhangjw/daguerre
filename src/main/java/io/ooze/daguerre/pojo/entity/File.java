package io.ooze.daguerre.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 文件记录表
 * </p>
 *
 * @author zhangjw
 * @since 2022-04-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class File extends GenericEntity {
    private static final long serialVersionUID = -6167395659396251730L;

    /**
     * 文件唯一标识
     */
    private String uid;
    /**
     * 文件扩展
     * 主要用来区分自定义尺寸图片，格式为 width_height，为空表示原文件，视频缩略图为 thumb
     */
    private String attach;
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
     * 分片大小
     */
    private Integer chunkSize;

    /**
     * 分片数量
     */
    private Integer chunkCount = 0;

    /**
     * 已上传分片索引
     */
    private Integer uploadedChunk;

    /**
     * 已上传文件大小
     */
    private Long uploadedLength;

    /**
     * 文件上传/合并完成标记
     */
    private Boolean completed = false;

    /**
     * 完成时间戳
     */
    private Long completeAt;

    /**
     * oss id
     */
    private String ossId;
    /**
     * 失效时间
     */
    private Long expireAt;

    private String accessKey;
}
