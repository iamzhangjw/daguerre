package io.ooze.daguerre.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("file_chunk")
public class FileChunk extends GenericEntity {
    private static final long serialVersionUID = 8382543561679928911L;
    /**
     * 文件id
     */
    private String uid;

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
     * 分片索引
     */
    private Integer chunkIndex;
    /**
     * 扩展数据
     */
    private String extra;
}
