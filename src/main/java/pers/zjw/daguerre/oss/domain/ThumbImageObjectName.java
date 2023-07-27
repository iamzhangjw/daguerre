package pers.zjw.daguerre.oss.domain;

import pers.zjw.daguerre.constant.ExpireMode;
import pers.zjw.daguerre.constant.FileType;
import pers.zjw.daguerre.constant.ThumbnailType;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 文件缩略图 ObjectName
 *
 * @author zhangjw
 * @date 2022/08/12 0012 17:56
 */
public class ThumbImageObjectName extends ObjectName {
    public static final String THUMB_FILE_EXTENSION = "webp";
    private final ThumbnailType thumbnailType;
    private final ObjectName objectName;

    public ThumbImageObjectName(ObjectName objectName, ThumbnailType thumbnailType) {
        Assert.isTrue(thumbnailType.sourceFileType() == objectName.type(),
                "type of source file mismatch");
        this.objectName = objectName;
        this.thumbnailType = thumbnailType;
    }

    public ThumbImageObjectName(FileMetadata metadata, ThumbnailType thumbnailType) {
        this(metadata.toObjectName(), thumbnailType);
    }

    public ThumbnailType thumbnailType() {
        return thumbnailType;
    }

    @Override
    public String id() {
        return objectName.id();
    }

    @Override
    public String original() {
        String sourceFilename = objectName.original();
        String fileExtension = FilenameUtils.getExtension(sourceFilename);
        return StringUtils.hasText(fileExtension)
                ? (sourceFilename.substring(0, sourceFilename.length()-fileExtension.length()-1)
                + "_" + thumbnailType.attach() + "." + THUMB_FILE_EXTENSION)
                : (sourceFilename + "_" + thumbnailType.attach() + "." + THUMB_FILE_EXTENSION);
    }

    @Override
    public String name() {
        return objectName.expireMode().directory() + "/" + type().directory() + "/" + objectName.id()
                + "_" + thumbnailType.attach() + "." + THUMB_FILE_EXTENSION;
    }

    @Override
    public FileType type() {
        return FileType.IMG;
    }

    @Override
    public String ossId() {
        return objectName.ossId();
    }

    @Override
    public ExpireMode expireMode() {
        return objectName.expireMode();
    }

    @Override
    public int expireDays() {
        return objectName.expireDays();
    }
}
