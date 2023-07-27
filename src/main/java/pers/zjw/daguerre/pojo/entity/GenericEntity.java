package pers.zjw.daguerre.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;

/**
 * generic entity
 *
 * @author zhangjw
 * @date 2022/04/01 0001 17:26
 */
@Data
public class GenericEntity implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 创建时间戳
     */
    private Long createAt;

    /**
     * 删除标记
     */
    @TableLogic
    private Boolean deleted = false;

    /**
     * 版本
     */
    private Long version;
}
