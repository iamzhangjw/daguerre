package io.ooze.daguerre.constant;

import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 厂商
 *
 * @author zhangjw
 * @date 2022/08/14 0014 9:46
 */
public enum Vendor {
    /**
     * 厂商
     */
    MINIO("MinIO"),
    ALIYUN("阿里云"),
    HUAWEICLOUD("华为云"),
    TENCENTCLOUD("腾讯云"),
    ;

    private String value;

    Vendor(String value) {
        this.value = value;
    }

    public final static Map<String, Vendor> ALL = Stream.of(values()).collect(
            Collectors.toMap(e -> e.name().toLowerCase(), e -> e));

    public String value() {
        return value;
    }

    public static Optional<Vendor> parse(String key) {
        if (StringUtils.hasText(key)) return Optional.ofNullable(ALL.get(key.toLowerCase()));
        return Optional.empty();
    }
}
