package io.ooze.daguerre.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.ooze.daguerre.constant.ThumbnailType;
import io.ooze.daguerre.mapper.FileMapper;
import io.ooze.daguerre.oss.domain.ChunkFileMetadata;
import io.ooze.daguerre.oss.domain.FileMetadata;
import io.ooze.daguerre.pojo.entity.File;
import io.ooze.daguerre.pojo.entity.FileChunk;
import io.ooze.daguerre.pojo.vo.AccessFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 文件记录表 服务类
 * </p>
 *
 * @author zhangjw
 * @since 2022-04-01
 */
@Slf4j
@Service
public class FileService extends ServiceImpl<FileMapper, File> {
    @Autowired
    private FileChunkService chunkService;
    @Autowired
    private FileMapper mapper;

    public File insertWholeFile(FileMetadata metadata, String accessKey) {
        return insertWholeFile(metadata, accessKey, null);
    }

    @Transactional(propagation= Propagation.REQUIRED, rollbackFor = Exception.class)
    public File insertWholeFile(FileMetadata metadata, String accessKey, ThumbnailType thumbnailType) {
        File file = metadata.toFile();
        file.setAccessKey(accessKey);
        if (null != thumbnailType) {
            file.setAttach(thumbnailType.attach());
        }
        file.setUploadedLength(file.getByteLength());
        file.setCompleteAt(file.getCreateAt());
        file.setCompleted(true);
        mapper.insert(file);
        return file;
    }

    @Transactional(propagation= Propagation.REQUIRED, rollbackFor = Exception.class)
    public File insertFileMetadata(FileMetadata fileMetadata, String accessKey, int chunkSize) {
        int chunkCount = (int)Math.ceil((double)fileMetadata.getLength() / chunkSize);
        File majorFile = fileMetadata.toFile();
        majorFile.setAccessKey(accessKey);
        majorFile.setChunkSize(chunkSize);
        majorFile.setUploadedChunk(0);
        majorFile.setChunkCount(chunkCount);
        majorFile.setUploadedLength(0L);
        mapper.insert(majorFile);
        return majorFile;
    }

    @Transactional(propagation= Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateCompletedFile(String uid, String accessKey, long version) {
        long now = System.currentTimeMillis();
        LambdaUpdateWrapper<File> update = Wrappers.lambdaUpdate(File.class)
                .eq(File::getUid, uid)
                .eq(File::getAccessKey, accessKey)
                .eq(File::getVersion, version)
                .set(File::getCompleted, true)
                .set(File::getCompleteAt, now)
                .set(File::getVersion, now);
        if (mapper.update(null, update) > 0) {
            chunkService.deleteAfterCompleted(uid);
        }
    }

    @Transactional(propagation= Propagation.REQUIRED, rollbackFor = Exception.class)
    public void insertFileChunkAndUpdateFileMetadata(ChunkFileMetadata chunkFileMetadata) {
        if (chunkService.exists(chunkFileMetadata.getId(), chunkFileMetadata.getIndex())) return;
        FileChunk fileChunk = chunkFileMetadata.toFileChunk();
        chunkService.insert(fileChunk);
        mapper.updateWhenChunkUploaded(fileChunk);
    }

    public File getByFileId(String uid, String accessKey) {
        return getByFileId(uid, accessKey, null);
    }

    public File getByFileId(String uid, String accessKey, String attach) {
        LambdaQueryWrapper<File> wrappers = Wrappers.lambdaQuery(File.class)
                .eq(File::getUid, uid)
                .eq(File::getAccessKey, accessKey);
        if (!StringUtils.hasText(attach)) {
            wrappers.isNull(File::getAttach);
        } else {
            wrappers.eq(File::getAttach, attach);
        }
        return mapper.selectOne(wrappers);
    }

    public AccessFile getFileIncludeUrl(String uid, String accessKey) {
        return mapper.getAccessFileByFileId(uid, accessKey);
    }

    public Collection<AccessFile> getFileIncludeUrl(Collection<String> uids, String accessKey, String attach) {
        return mapper.getAccessFile(uids, accessKey, attach);
    }

    public List<File> scrollExpired(Long id, long expireAt, int limit) {
        LambdaQueryWrapper<File> wrappers = Wrappers.lambdaQuery(File.class)
                .between(File::getExpireAt, 0, expireAt);
        if (null != id) {
            wrappers.gt(File::getId, id);
        }
        wrappers.orderByAsc(File::getId).last("limit " + limit);
        return mapper.selectList(wrappers);
    }

    public void deleteByIds(Collection<Long> ids) {
        long now = System.currentTimeMillis();
        mapper.update(null, Wrappers.lambdaUpdate(File.class)
                .in(File::getId, ids)
                .eq(File::getDeleted, 0)
                .set(File::getDeleted, 1)
                .set(File::getVersion, now));
    }
}
