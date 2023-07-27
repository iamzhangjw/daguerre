package io.ooze.daguerre.constant;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 摘要算法
 *
 * @author zhangjw
 * @date 2022/03/02 0002 11:03
 */
public enum Algorithm {
    /**
     * 摘要算法
     */
    MD5("消息摘要算法") {
        @Override
        public String digest(String secret, String plainText) {
            if (!StringUtils.hasText(secret) || !StringUtils.hasText(plainText)) return plainText;
            return DigestUtils.md5Hex(secret + plainText);
        }
    },
    SHA1("安全散列算法1") {
        @Override
        public String digest(String secret, String plainText) {
            if (!StringUtils.hasText(secret) || !StringUtils.hasText(plainText)) return plainText;
            return DigestUtils.sha1Hex(secret + plainText);
        }
    },
    ;

    private final String value;
    Algorithm(String value) {
        this.value = value;
    }

    public final static Map<String, Algorithm> ALL = Stream.of(values()).collect(
            Collectors.toMap(e -> e.name().toLowerCase(), e -> e));

    public String value() {
        return value;
    }

    public static Optional<Algorithm> parse(String key) {
        if (StringUtils.hasText(key)) return Optional.ofNullable(ALL.get(key.toLowerCase()));
        return Optional.empty();
    }

    public abstract String digest(String secret, String plainText);
}
