package io.ooze.daguerre.constant;

import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Thumbnail 类型枚举
 *
 * @author zhangjw
 * @date 2022/08/14 0014 9:46
 */
public enum ThumbnailType {
    /**
     * 缩略图
     */
    THUMBNAIL(FileType.IMG, "thumb"),
    COVER(FileType.PLAY, "cover"),
    ;

    private final FileType source;
    private final String attach;

    ThumbnailType(FileType source, String attach) {
        this.source = source;
        this.attach = attach;
    }

    public String attach() {
        return attach;
    }

    public FileType sourceFileType() {
        return source;
    }

    public FileType fileType() {
        return FileType.IMG;
    }

    public final static Map<String, ThumbnailType> ALL = Stream.of(values()).collect(
            Collectors.toMap(ThumbnailType::attach, e -> e));

    public static Optional<ThumbnailType> parse(String attach) {
        if (StringUtils.hasText(attach)) return Optional.ofNullable(ALL.get(attach.toLowerCase()));
        return Optional.empty();
    }
}
