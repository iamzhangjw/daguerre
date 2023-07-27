package io.ooze.daguerre.oss.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import io.ooze.daguerre.constant.FileType;
import io.ooze.daguerre.pojo.entity.FileChunk;
import io.ooze.daguerre.utils.JsonParser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * ChunkFileMetadata
 *
 * @author zhangjw
 * @date 2022/09/24 0024 13:43
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class ChunkFileMetadata extends FileMetadata {
    private final int index;
    private final Map<String, Object> extra;

    public ChunkFileMetadata(FileMetadata metadata, int index, Map<String, Object> extra) {
        super(metadata.getId(), metadata.getFilename(), metadata.getBucketName(), metadata.getObjectName(),
                metadata.getLength(), metadata.getType(), 1, System.currentTimeMillis(), metadata.getOssId());
        this.index = index;
        this.extra = Collections.unmodifiableMap(extra);
    }

    public ChunkFileMetadata(FileMetadata metadata, int index) {
        super(metadata.getId(), metadata.getFilename(), metadata.getBucketName(), metadata.getObjectName(),
                metadata.getLength(), metadata.getType(), 1, System.currentTimeMillis(), metadata.getOssId());
        this.index = index;
        this.extra = Collections.emptyMap();
    }

    public ChunkFileMetadata(FileChunk fileChunk) {
        super(fileChunk.getUid(), fileChunk.getOriginalName(),
                fileChunk.getBucketName(), fileChunk.getStorePath(),
                fileChunk.getByteLength(), FileType.parse(fileChunk.getOriginalName()),
                1, fileChunk.getVersion());
        this.index = fileChunk.getChunkIndex();
        if (!StringUtils.hasText(fileChunk.getExtra())) {
            this.extra = Collections.emptyMap();
        } else {
            this.extra = JsonParser.toObject(fileChunk.getExtra(), new TypeReference<Map<String, Object>>() {
            });
        }
    }

    public FileChunk toFileChunk() {
        FileChunk chunk = new FileChunk();
        chunk.setUid(getId());
        chunk.setBucketName(getBucketName());
        chunk.setOriginalName(getFilename());
        chunk.setStorePath(getObjectName());
        chunk.setType(getType().name().toLowerCase());
        chunk.setByteLength(getLength());
        chunk.setChunkIndex(index);
        chunk.setExtra(JsonParser.toString(extra));
        chunk.setCreateAt(getLastModified());
        chunk.setVersion(getLastModified());
        return chunk;
    }
}
