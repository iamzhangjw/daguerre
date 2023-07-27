package io.ooze.daguerre.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.ooze.daguerre.mapper.FileUrlMapper;
import io.ooze.daguerre.pojo.entity.FileUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件访问url记录表 服务类
 * </p>
 *
 * @author zhangjw
 * @since 2022-04-01
 */
@Slf4j
@Service
public class FileUrlService extends ServiceImpl<FileUrlMapper, FileUrl> {
    @Autowired
    private FileUrlMapper mapper;

    public Long insert(String uid, String url, Long expireAt) {
        return insert(uid, null, url, expireAt);
    }

    public Long insert(String uid, String attach, String url, Long expireAt) {
        FileUrl fileUrl = new FileUrl();
        fileUrl.setUid(uid);
        String[] urlArr = url.split("\\?");
        fileUrl.setUrl(urlArr[0]);
        fileUrl.setAttach(attach);
        fileUrl.setUrlQueryParams(urlArr[1]);
        fileUrl.setExpireAt(expireAt);
        fileUrl.setCreateAt(System.currentTimeMillis());
        fileUrl.setVersion(fileUrl.getCreateAt());
        mapper.insert(fileUrl);
        return fileUrl.getId();
    }

    public void update(String uid, String url, Long expireAt) {
        update(uid, null, url, expireAt);
    }

    public void update(String uid, String attach, String url, Long expireAt) {
        String[] urlArr = url.split("\\?");
        LambdaUpdateWrapper<FileUrl> update = Wrappers.lambdaUpdate(FileUrl.class)
                .eq(FileUrl::getUid, uid)
                .eq(FileUrl::getAttach, attach)
                .set(FileUrl::getUrl, urlArr[0])
                .set(FileUrl::getUrlQueryParams, urlArr[1])
                .set(FileUrl::getExpireAt, expireAt);
        mapper.update(null, update);
    }
}
