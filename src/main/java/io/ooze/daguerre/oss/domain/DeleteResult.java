package io.ooze.daguerre.oss.domain;

import lombok.Data;

/**
 * DeleteResult
 *
 * @author zhangjw
 * @date 2022/09/23 0023 17:04
 */
@Data
public class DeleteResult {
    private String bucketName;
    private String objectName;
    private boolean success;

    public DeleteResult(String bucketName, String objectName, boolean success) {
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.success = success;
    }
}
