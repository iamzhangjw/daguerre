package pers.zjw.daguerre.constant;

import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ExpireMode
 *
 * @author zhangjw
 * @date 2022/10/25 0025 16:07
 */
public enum ExpireMode {
    /**
     * 目录
     */
    WEEK("week", 7),
    MONTH("month", 30),
    QUARTER("quarter", 90),
    HALF_YEAR("half_year", 180),
    YEAR("year", 360),
    DEFAULT("default", -1),
    ;

    private final String dir;
    private final int expireDays;

    ExpireMode(String dir, int expireDays) {
        this.dir = dir;
        this.expireDays = expireDays;
    }

    public final static Map<String, ExpireMode> ALL = Stream.of(values()).collect(
            Collectors.toMap(e -> e.name().toLowerCase(), e -> e));

    public String directory() {
        return dir;
    }

    public int expireDays() {
        return expireDays;
    }

    public static ExpireMode parse(String key) {
        if (StringUtils.hasText(key)) {
            ExpireMode directory = ALL.get(key.toLowerCase());
            return null == directory ? DEFAULT : directory;
        }
        return DEFAULT;
    }

    public static ExpireMode parse(int expireDays) {
        return ALL.values().stream().filter(e -> expireDays == e.expireDays()).findFirst().orElse(DEFAULT);
    }
}
