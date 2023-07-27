package io.ooze.daguerre.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 文件访问url记录表
 * </p>
 *
 * @author zhangjw
 * @since 2022-04-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_url")
public class FileUrl extends GenericEntity {
    private static final long serialVersionUID = -7642359554183886676L;
    /**
     * 文件id
     */
    private String uid;

    private String attach;

    /**
     * 文件url
     */
    private String url;

    /**
     * 文件url访问参数
     */
    private String urlQueryParams;

    /**
     * 到期时间戳
     */
    private Long expireAt;
}
