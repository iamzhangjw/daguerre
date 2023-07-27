package pers.zjw.daguerre.oss;

import pers.zjw.daguerre.exception.OssException;
import pers.zjw.daguerre.oss.domain.ObjectName;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * AbstractOssApi
 *
 * @author zhangjw
 * @date 2022/09/23 0023 13:58
 */
public abstract class AbstractOssApi implements OssApi {
    protected static final DateFormat GMT_DF = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    static {
        GMT_DF.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    protected void createBucketIfMissing(String bucket) {
        if (bucketExists(bucket)) return;
        createBucket(bucket);
    }

    protected Map<String, String> userMetadata(ObjectName objectName) {
        Map<String, String> map = new HashMap<>();
        map.put("id", objectName.id());
        map.put("name", objectName.original());
        map.put("type", objectName.type().name().toLowerCase());
        return map;
    }

    @Override
    public String startMultiChunkUpload(String bucket, ObjectName objectName) throws OssException {
        createBucketIfMissing(bucket);
        return null;
    }

    @Override
    public String url(String bucket, String objectName) throws OssException {
        return url(bucket, objectName, 2);
    }

    @Override
    public InputStream download(String bucket, String objectName) throws OssException {
        return download(bucket, objectName, -1, -1);
    }

    @Override
    public InputStream download(String bucket, String objectName, long offset) throws OssException {
        return download(bucket, objectName, offset, -1);
    }
}
