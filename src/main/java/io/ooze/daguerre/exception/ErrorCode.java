package io.ooze.daguerre.exception;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 错误码
 *
 * @date 2021/07/28 0028 14:51
 * @author zhangjw
 */
public enum ErrorCode {
    /**
     * 成功
     */
    SUCCESS(200, "success"),
    INVOKE_FAILED(500, "执行失败"),

    /**
     * 调用
     *
     */
    MISSING_KEY(10001, "缺少 access key"),
    ACCESS_KEY_ABNORMAL(10002, "access key 异常，请检查申请"),
    ACCESS_DENIED(10100, "访问被拒绝"),
    AUTHORIZE_FAILED(10101, "鉴权失败"),

    TIMESTAMP_EXPIRED(10200, "请求时间戳过期"),
    INVALID_SIGN(10201, "签名无效"),
    IP_NOT_ALLOWED_ACCESS(10202, "客户端 IP 不被允许调用服务，请检查对应服务账号的白名单"),
    WRONG_CERT(10203, "证书错误或失效，请检查对应服务账号的证书"),


    MISMATCH_PARAM(11000, "请求参数非法或缺失，请检查请求参数"),
    ILLEGAL_REQUEST(11001, "非法请求，请检查 URL 是否正确"),

    /**
     * 业务异常
     */
    READ_STREAM_FAILED(20000, "读取流失败"),
    FILE_SIZE_MISMATCH(20001, "文件大小不匹配"),
    RESOURCE_NOT_FOUND(20002, "找不到资源")
    ;


    private final int code;
    private final String msg;

    private final static Map<Integer,ErrorCode> CODES = Stream.of(values()).collect(
            Collectors.toMap(ErrorCode::code, e -> e));

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static Optional<ErrorCode> parse(int code) {
        return Optional.ofNullable(CODES.get(code));
    }

    public int code() {
        return code;
    }

    public String msg() {
        return msg;
    }
}
