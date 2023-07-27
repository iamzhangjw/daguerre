package pers.zjw.daguerre.oss;

import pers.zjw.daguerre.constant.Vendor;
import pers.zjw.daguerre.pojo.entity.Credential;
import lombok.Data;

import java.util.Optional;

/**
 * OssProperties
 *
 * @author zhangjw
 * @date 2022/03/30 0030 16:45
 */
@Data
public class OssProperties {
    private Vendor vendor;
    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;

    public static OssProperties from(Credential credential) {
        Optional<Vendor> vendor = Vendor.parse(credential.getOssVendor());
        String msg = "unsupported vendor: " + credential.getOssVendor();
        if (!vendor.isPresent()) throw new IllegalArgumentException(msg);
        OssProperties properties = new OssProperties();
        properties.vendor = vendor.get();
        properties.endpoint = credential.getEndpoint();
        properties.region = credential.getRegion();
        properties.accessKey = credential.getOssAccessKey();
        properties.secretKey = credential.getOssSecretKey();
        return properties;
    }
}
