package io.ooze.daguerre.oss.domain;

import io.ooze.daguerre.constant.ExpireMode;
import io.ooze.daguerre.constant.FileType;
import org.springframework.util.StringUtils;

/**
 * ObjectNameFactory
 *
 * @author zhangjw
 * @date 2022/11/01 0001 13:36
 */
public class ObjectNameFactory {
    private final IdGenerator idGenerator;

    public ObjectNameFactory(String typeSpec) {
        idGenerator = IdGenerators.of(typeSpec);
    }

    public ObjectName create(String filename, FileType fileType) {
        return ObjectName.builder().with(idGenerator.createId(), filename, fileType).build();
    }

    public ObjectName create(String filename, FileType fileType, int expireDays) {
        return ObjectName.builder().with(idGenerator.createId(), filename, fileType, expireDays).build();
    }

    public ObjectName create(String filename, FileType fileType, ExpireMode expireMode) {
        return ObjectName.builder().with(idGenerator.createId(), filename, fileType, expireMode).build();
    }

    public ObjectName create(String id, String objectName, String filename,
                             FileType fileType, String ossId, int expireDays) {
        if (!idGenerator.checkId(id)) {
            throw new IllegalArgumentException(id + " not a valid id");
        }
        if (StringUtils.hasText(ossId)) {
            return ObjectName.builder().with(id, objectName, filename, fileType, ossId, expireDays).build();
        }
        return ObjectName.builder().with(id, objectName, filename, fileType, expireDays).build();
    }

    public ObjectName create(String id, String objectName, String filename,
                             FileType fileType, String ossId, ExpireMode expireMode) {
        if (!idGenerator.checkId(id)) {
            throw new IllegalArgumentException(id + " not a valid id");
        }
        if (StringUtils.hasText(ossId)) {
            return ObjectName.builder().with(id, objectName, filename, fileType, ossId, expireMode).build();
        }
        return ObjectName.builder().with(id, objectName, filename, fileType, expireMode).build();
    }
}
