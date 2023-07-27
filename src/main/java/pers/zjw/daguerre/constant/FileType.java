package pers.zjw.daguerre.constant;

import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * file type enum
 *
 * @author zhangjw
 * @date 2022/03/30 0030 19:56
 */
public enum FileType {
    /**
     * 文件类型枚举
     */
    DOC("文档文件", "doc", Pattern.compile("doc|docx|xls|xlsx|ppt|pptx|md|txt|csv|log|pdf")),
    EXE("可执行文件", "exe", Pattern.compile("exe|apk|ipa|rpm|deb|sh")),
    IMG("图片文件", "img", Pattern.compile("bmp|jpg|png|gif|tif|webp|pcx|jpeg|tga|svg|psd|pcd|dxf|eps|epsf")),
    PLAY("可播放文件", "play", Pattern.compile("wav|wma|wmv|aif|aiff|aifc|flac|m4a|au|mp3|ra|rm|ram|mid|rmi|mp4|mov|flv|avi|webm|mkv|rmvb|3gp|m4v|vob|asf|mpeg|mpg|ts|dat")),
    UNKNOWN("未知文件", "ukn", Pattern.compile("[a-zA-Z0-9]+"))
    ;

    private final String text;
    private final String dir;
    private final Pattern extPattern;


    FileType(String text, String dir, Pattern pattern) {
        this.text = text;
        this.dir = dir;
        this.extPattern = pattern;
    }

    public final static List<FileType> recognizableTypes = Stream.of(values())
            .filter(e -> !Objects.equals(e, UNKNOWN))
            .collect(Collectors.toList());

    public String directory() {
        return dir;
    }

    public boolean match(String ext) {
        return extPattern.matcher(ext.toLowerCase()).matches();
    }

    public static FileType parse(String filename) {
        if (!StringUtils.hasText(filename)) return UNKNOWN;
        String ext = FilenameUtils.getExtension(filename);
        if (!StringUtils.hasText(ext)) return UNKNOWN;
        for (FileType value : recognizableTypes) {
            if (value.match(ext)) return value;
        }
        return UNKNOWN;
    }
}
