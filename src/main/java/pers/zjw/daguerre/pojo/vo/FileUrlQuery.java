package pers.zjw.daguerre.pojo.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Collection;

/**
 * FileUrlQuery
 *
 * @author zhangjw
 * @date 2022/08/24 0024 13:33
 */
@Data
public class FileUrlQuery {
    @NotEmpty
    private Collection<String> fileIds;

    private Integer expireMinutes;

    private String attach;
}
