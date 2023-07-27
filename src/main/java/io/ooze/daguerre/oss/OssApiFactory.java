package io.ooze.daguerre.oss;

import io.ooze.daguerre.constant.Vendor;

/**
 * OssApiFactory
 *
 * @author zhangjw
 * @date 2022/09/23 0023 14:36
 */
public class OssApiFactory {
    public static OssApi create(OssProperties properties) {
        if (Vendor.MINIO == properties.getVendor()) {
            return new MinioApi(properties);
        } else if (Vendor.ALIYUN == properties.getVendor()) {
            return new AliyunApi(properties);
        } else if (Vendor.HUAWEICLOUD == properties.getVendor()) {
            return new HuaweiCloudApi(properties);
        } else if (Vendor.TENCENTCLOUD == properties.getVendor()) {
            return new TencentCloudApi(properties);
        }
        throw new UnsupportedOperationException("unsupported vendor: " + properties.getVendor().name());
    }
}
