package io.ooze.daguerre.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.ooze.daguerre.mapper.FileChunkMapper;
import io.ooze.daguerre.pojo.entity.FileChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class FileChunkService extends ServiceImpl<FileChunkMapper, FileChunk> {
    @Autowired
    private FileChunkMapper mapper;

    public FileChunk insert(FileChunk entity) {
        mapper.insert(entity);
        return entity;
    }

    public void deleteAfterCompleted(String uid) {
        long now = System.currentTimeMillis();
        mapper.update(null, Wrappers.lambdaUpdate(FileChunk.class)
                .eq(FileChunk::getUid, uid)
                .eq(FileChunk::getDeleted, 0)
                .set(FileChunk::getDeleted, 1)
                .set(FileChunk::getVersion, now));
    }

    public List<FileChunk> listByFileId(String uid) {
        return mapper.selectList(Wrappers.lambdaQuery(FileChunk.class)
                .eq(FileChunk::getUid, uid)
                .orderByDesc(FileChunk::getChunkIndex));
    }

    public boolean exists(String uid, int index) {
        return super.count(Wrappers.lambdaQuery(FileChunk.class)
                .eq(FileChunk::getUid, uid)
                .eq(FileChunk::getChunkIndex, index)) > 0;
    }
}
