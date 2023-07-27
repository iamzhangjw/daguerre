package pers.zjw.daguerre.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.StringUtils;

/**
 * Credential
 *
 * @author zhangjw
 * @date 2022/08/20 0020 17:26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Credential extends GenericEntity {
    private static final long serialVersionUID = 2445551115768616140L;

    private String accessKey;
    private String accessSecret;
    private String algorithm;
    private String endpoint;
    private String region;
    private String bucket;
    private String ossVendor;
    private String ossAccessKey;
    private String ossSecretKey;
    private Long modifyAt;

    public static String createAccessKey() {
        String raw = RandomStringUtils.randomAlphanumeric(10);
        return raw + complement(raw);
    }

    public static boolean validateAccessKey(String accessKey) {
        if (!StringUtils.hasText(accessKey) || accessKey.length() != 14) return false;
        String raw = accessKey.substring(0, 10);
        return complement(raw).equals(accessKey.substring(10));
    }

    private static String complement(String raw) {
        String hex = Integer.toHexString(raw.hashCode());
        if (hex.length() >= 4) return hex.substring(0, 4);
        return (hex + "zeus").substring(0, 4);
    }

    public static void main(String[] args) {
        System.out.println(Credential.createAccessKey());
        System.out.println(RandomStringUtils.randomAlphanumeric(32));
    }
}
