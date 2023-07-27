package pers.zjw.daguerre.oss;

import pers.zjw.daguerre.pojo.entity.Credential;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OssHolder
 *
 * @author zhangjw
 * @date 2022/08/21 0021 18:51
 */
public class OssHolder {
    private final static ThreadLocal<Credential> credentialTl = new ThreadLocal<>();
    private final static Map<String, OssApi> apiMap = new ConcurrentHashMap<>();

    public static Credential credential() {
        return credentialTl.get();
    }

    public static String bucket() {
        return credentialTl.get().getBucket();
    }

    public static String accessKey() {
        return credentialTl.get().getAccessKey();
    }

    public static void put(Credential credential) {
        credentialTl.remove();
        credentialTl.set(credential);
    }

    public static OssApi getApi() {
        return getApi(credential());
    }

    public static OssApi getApi(Credential credential) {
        return apiMap.computeIfAbsent(
                credential.getAccessKey(),
                key -> OssApiFactory.create(OssProperties.from(credential)));
    }

    public static void close() {
        apiMap.values().forEach(OssApi::close);
    }
}
