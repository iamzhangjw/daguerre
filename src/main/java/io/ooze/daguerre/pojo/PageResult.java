package io.ooze.daguerre.pojo;

import lombok.Data;

import java.util.List;

/**
 * web 响应
 *
 * @date 2021/07/31 0031 14:04
 * @author zhangjw
 */
@Data
public class PageResult<T> {
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<T> list;

    private PageResult(long total, int pageNum, int pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
    }

    private PageResult(long total, int pageSize, List<T> list) {
        this.total = total;
        this.pageSize = pageSize;
        this.list = list;
    }

    private PageResult(int pageSize, List<T> list) {
        this.pageSize = pageSize;
        this.list = list;
    }

    public static <T> PageResult<T> create(long total, int pageNum, int pageSize, List<T> list) {
        return new PageResult<>(total, pageNum, pageSize, list);
    }

    public static <T> PageResult<T> create(long total, int pageSize, List<T> list) {
        return new PageResult<>(total, pageSize, list);
    }

    public static <T> PageResult<T> create(int pageSize, List<T> list) {
        return new PageResult<>(pageSize, list);
    }
}
