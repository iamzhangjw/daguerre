package pers.zjw.daguerre.task;

import pers.zjw.daguerre.constant.Vendor;
import pers.zjw.daguerre.oss.OssApi;
import pers.zjw.daguerre.oss.OssHolder;
import pers.zjw.daguerre.pojo.entity.Credential;
import pers.zjw.daguerre.pojo.entity.File;
import pers.zjw.daguerre.service.CredentialService;
import pers.zjw.daguerre.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClearExpiredFileTask
 *
 * @author zhangjw
 * @date 2022/10/28 0028 17:34
 */
@Slf4j
@Component
public class ClearExpiredFileTask {
    @Autowired
    private FileService fileService;
    @Autowired
    private CredentialService credentialService;

    private final Map<String, Credential> credentialMap = new ConcurrentHashMap<>();

    /**
     * 每周五 22点执行
     */
    @Scheduled(cron= "0 0 22 ? * 5")
    public void run() {
        long startId = 0, expireAt = System.currentTimeMillis();
        int limit = 1000, size = 0;
        do {
            List<File> list = fileService.scrollExpired(startId, expireAt, limit);
            if (CollectionUtils.isEmpty(list)) break;
            startId = list.get(list.size()-1).getId();
            size = list.size();
            List<Long> deleteIds = new LinkedList<>();
            for (File file : list) {
                if (deleteFile(file)) {
                    deleteIds.add(file.getId());
                }
            }
            if (!CollectionUtils.isEmpty(deleteIds)) {
                fileService.deleteByIds(deleteIds);
            }
        } while(size == limit);
    }

    private boolean deleteFile(File file) {
        Credential credential = credentialMap.computeIfAbsent(
                file.getAccessKey(),
                key -> credentialService.getByAccessKey(key));
        Optional<Vendor> vendor = Vendor.parse(credential.getOssVendor());
        if (!vendor.isPresent()) return false;
        OssApi api = OssHolder.getApi(credential);
        if (!api.exists(file.getBucketName(), file.getStorePath())) return true;
        return api.delete(file.getBucketName(), file.getStorePath());
    }
}
