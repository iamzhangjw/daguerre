package pers.zjw.daguerre.oss.domain;

import pers.zjw.daguerre.constant.ExpireMode;
import pers.zjw.daguerre.constant.FileType;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * object name
 *
 * @author zhangjw
 * @date 2022/05/01 0001 14:02
 */
public class ObjectName {
    private String id;
    private String name;
    private String original;
    private FileType type;
    private String ossId;
    private ExpireMode expireMode;
    private Integer expireDays;

    public String id() {
        return id;
    }

    public String original() {
        return original;
    }

    public String name() {
        return name;
    }

    public FileType type() {
        return type;
    }

    public String ossId() {
        return ossId;
    }

    public ExpireMode expireMode() {
        return expireMode;
    }

    public int expireDays() {
        return expireDays;
    }

    public ObjectName chunk(int index) {
        Assert.isTrue(index > -1, "index must not be negative");
        return ObjectName.builder().with(id(), name()+"."+index, original(), type(), 1).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * refer to MinIO
     */
    public static final class Builder {
        protected List<Consumer<ObjectName>> operations;

        public Builder() {
            this.operations = new ArrayList<>();
        }

        public Builder with(String id, String filename, FileType fileType) {
            return with(id, filename, fileType, ExpireMode.DEFAULT);
        }

        public Builder with(String id, String filename, FileType fileType, ExpireMode expireMode) {
            return with(id, filename, fileType, expireMode, expireMode.expireDays());
        }

        public Builder with(String id, String filename, FileType fileType, int expireDays) {
            return with(id, filename, fileType, ExpireMode.DEFAULT, expireDays);
        }

        public Builder with(String id, String filename, FileType fileType, ExpireMode expireMode, int expireDays) {
            String ext = FilenameUtils.getExtension(filename);
            String objectName = expireMode.directory() + "/" + fileType.directory() + "/" + id
                    + (StringUtils.hasText(ext) ? ("."+ext) : "");
            return with(id, objectName, filename, fileType, null, expireMode, expireDays);
        }

        public Builder with(String id, String objectName, String filename, FileType fileType) {
            return with(id, objectName, filename, fileType, ExpireMode.DEFAULT);
        }

        public Builder with(String id, String objectName, String filename, FileType fileType, ExpireMode expireMode) {
            return with(id, objectName, filename, fileType, null, expireMode);
        }

        public Builder with(String id, String objectName, String filename, FileType fileType, int expireDays) {
            return with(id, objectName, filename, fileType, null, expireDays);
        }

        public Builder with(String id, String objectName, String filename, FileType fileType, String ossId) {
            return with(id, objectName, filename, fileType, ossId, ExpireMode.DEFAULT);
        }

        public Builder with(String id, String objectName, String filename, FileType fileType, String ossId, ExpireMode expireMode) {
            return with(id, objectName, filename, fileType, ossId, expireMode, expireMode.expireDays());
        }

        public Builder with(String id, String objectName, String filename, FileType fileType, String ossId, int expireDays) {
            return with(id, objectName, filename, fileType, ossId, ExpireMode.DEFAULT, expireDays);
        }

        public Builder with(String id, String objectName, String filename, FileType fileType,
                            String ossId, ExpireMode expireMode, int expireDays) {
            operations.add(args -> args.id = id);
            operations.add(args -> args.name = objectName);
            operations.add(args -> args.original = filename);
            operations.add(args -> args.type = fileType);
            if (StringUtils.hasText(ossId)) {
                operations.add(args -> args.ossId = ossId);
            }
            operations.add(args -> args.expireMode = expireMode);
            operations.add(args -> args.expireDays = expireDays);
            return this;
        }

        private void validate(ObjectName args) {
            validateNotEmptyString(args.id, "id");
            validateNotEmptyString(args.name, "objectName");
            validateNotEmptyString(args.original, "filename");
            validateNotNull(args.type, "fileType");
            validateNotNull(args.expireMode, "expireMode");
            validateNotNull(args.expireDays, "expireDays");
        }

        private void validateNotNull(Object arg, String argName) {
            if (arg == null) {
                throw new IllegalArgumentException(argName + " must not be null.");
            }
        }

        private void validateNotEmptyString(String arg, String argName) {
            validateNotNull(arg, argName);
            if (arg.isEmpty()) {
                throw new IllegalArgumentException(argName + " must be a non-empty string.");
            }
        }

        private ObjectName newInstance() {
            try {
                for (Constructor<?> constructor :
                        this.getClass().getEnclosingClass().getDeclaredConstructors()) {
                    if (constructor.getParameterCount() == 0) {
                        return (ObjectName) constructor.newInstance();
                    }
                }
                throw new RuntimeException(this.getClass().getEnclosingClass() + " must have no argument constructor");
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }

        public ObjectName build() throws IllegalArgumentException {
            ObjectName args = newInstance();
            operations.forEach(operation -> operation.accept(args));
            validate(args);
            return args;
        }
    }
}
