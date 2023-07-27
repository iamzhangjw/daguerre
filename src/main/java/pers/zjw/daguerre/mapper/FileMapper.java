package pers.zjw.daguerre.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import pers.zjw.daguerre.pojo.entity.File;
import pers.zjw.daguerre.pojo.entity.FileChunk;
import pers.zjw.daguerre.pojo.vo.AccessFile;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * <p>
 * 文件记录表 Mapper 接口
 * </p>
 *
 * @author zhangjw
 * @since 2022-05-01
 */
public interface FileMapper extends BaseMapper<File> {
    AccessFile getAccessFileByFileId(@Param("uid") String uid, @Param("accessKey") String accessKey);

    Collection<AccessFile> getAccessFile(@Param("uids") Collection<String> uids, @Param("accessKey") String accessKey, @Param("attach") String attach);

    void updateWhenChunkUploaded(@Param("fileChunk") FileChunk fileChunk);
}
