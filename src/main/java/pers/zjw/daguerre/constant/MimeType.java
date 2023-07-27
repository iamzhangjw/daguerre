package pers.zjw.daguerre.constant;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;

/**
 * write some class description
 *
 * @author zhangjw
 * @date 2022/05/03 0003 11:43
 */
public interface MimeType {

    /**
     * 根据文件名和文件类型解析 content-type
     * @param filename 文件名
     * @param fileType 文件类型
     * @return
     */
    static String parse(String filename, FileType fileType) {
        String ext = FilenameUtils.getExtension(filename);
        if (FileType.IMG == fileType) {
            if ("png".equalsIgnoreCase(ext)) {
                return MediaType.IMAGE_PNG_VALUE;
            } else if ("jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext)) {
                return MediaType.IMAGE_JPEG_VALUE;
            } else if ("gif".equalsIgnoreCase(ext)) {
                return MediaType.IMAGE_GIF_VALUE;
            } else if ("svg".equalsIgnoreCase(ext)) {
                return "image/svg+xml";
            } else if ("bmp".equalsIgnoreCase(ext)) {
                return "image/bmp";
            } else if ("webp".equalsIgnoreCase(ext)) {
                return "image/webp";
            } else if ("ico".equalsIgnoreCase(ext)) {
                return "image/x-icon";
            }
        } else if (FileType.PLAY == fileType) {
            if ("wav".equalsIgnoreCase(ext)) {
                return "audio/x-wav";
            } else if ("wma".equalsIgnoreCase(ext)) {
                return "audio/x-ms-wma";
            } else if ("mp3".equalsIgnoreCase(ext)) {
                return "audio/mp3";
            } else if ("wmv".equalsIgnoreCase(ext)) {
                return "video/x-ms-wmv";
            } else if ("mp4".equalsIgnoreCase(ext)) {
                return "video/mp4";
            } else if ("mpeg".equalsIgnoreCase(ext)) {
                return "video/mpeg";
            } else if ("avi".equalsIgnoreCase(ext)) {
                return "video/x-msvideo";
            } else if ("flv".equalsIgnoreCase(ext)) {
                return "video/x-flv";
            } else if ("m3u8".equalsIgnoreCase(ext)) {
                return "application/x-mpegURL";
            } else if ("ts".equalsIgnoreCase(ext)) {
                return "video/MP2T";
            } else if ("3gp".equalsIgnoreCase(ext)) {
                return "video/3gpp";
            } else if ("mov".equalsIgnoreCase(ext)) {
                return "video/quicktime";
            } else if ("ogv".equalsIgnoreCase(ext) || "oga".equalsIgnoreCase(ext)) {
                return "video/ogg";
            } else if ("ogx".equalsIgnoreCase(ext)) {
                return "application/ogg";
            } else if ("rmvb".equalsIgnoreCase(ext)) {
                return "application/vnd.rn-realmedia-vbr";
            } else if ("swf".equalsIgnoreCase(ext)) {
                return "application/x-shockwave-flash";
            } else if ("weba".equalsIgnoreCase(ext) || "webm".equalsIgnoreCase(ext)) {
                return "audio/webm";
            } else if ("aac".equalsIgnoreCase(ext)) {
                return "audio/aac";
            } else if ("mid".equalsIgnoreCase(ext)) {
                return "audio/midi";
            } else if ("midi".equalsIgnoreCase(ext)) {
                return "audio/x-midi";
            }
        } else if (FileType.DOC == fileType) {
            if ("txt".equalsIgnoreCase(ext)) {
                return "text/plain";
            } else if ("html".equalsIgnoreCase(ext) || "htm".equalsIgnoreCase(ext)) {
                return "text/html";
            } else if ("xml".equalsIgnoreCase(ext)) {
                return "text/xml";
            } else if ("xhtml".equalsIgnoreCase(ext)) {
                return "application/xhtml+xml";
            } else if ("json".equalsIgnoreCase(ext)) {
                return "application/json";
            } else if ("pdf".equalsIgnoreCase(ext)) {
                return "application/pdf";
            } else if ("doc".equalsIgnoreCase(ext) || "docx".equalsIgnoreCase(ext)) {
                return "application/msword";
            } else if ("xls".equalsIgnoreCase(ext) || "xlsx".equalsIgnoreCase(ext)) {
                return "application/vnd.ms-excel";
            } else if ("ppt".equalsIgnoreCase(ext) || "pptx".equalsIgnoreCase(ext)) {
                return "application/vnd.ms-powerpoint";
            } else if ("css".equalsIgnoreCase(ext)) {
                return "text/css";
            } else if ("js".equalsIgnoreCase(ext)) {
                return "application/javascript";
            } else if ("csv".equalsIgnoreCase(ext)) {
                return "text/csv";
            } else {
                return "text/plain";
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
