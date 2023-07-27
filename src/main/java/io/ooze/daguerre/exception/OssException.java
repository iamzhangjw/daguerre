package io.ooze.daguerre.exception;

import io.ooze.daguerre.constant.Vendor;

/**
 * oss exception
 *
 * @author zhangjw
 * @date 2022/03/30 0030 18:52
 */
public class OssException extends BaseException {
    private Vendor vendor;
    private String bucket;
    private String objectName;
    private String filename;

    public OssException() {
        super();
    }

    public OssException(Vendor vendor, String msg) {
        super(msg);
    }

    public OssException(Vendor vendor, String msg, String bucket, String objectName, String filename) {
        super(msg);
        this.vendor = vendor;
        this.bucket = bucket;
        this.objectName = objectName;
        this.filename = filename;
    }

    public OssException(Vendor vendor, String msg, String bucket, String objectName, String filename, Throwable throwable) {
        super(msg, throwable);
        this.vendor = vendor;
        this.bucket = bucket;
        this.objectName = objectName;
        this.filename = filename;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " with vendor=" + vendor.name()
                + ", source=" + filename
                + ", object=" + objectName
                + ", bucket=" + bucket;
    }
}
