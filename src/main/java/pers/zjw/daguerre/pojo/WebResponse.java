package pers.zjw.daguerre.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import pers.zjw.daguerre.exception.ErrorCode;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * web 响应
 *
 * @date 2021/07/31 0031 13:59
 * @author zhangjw
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebResponse<T> implements Serializable {
    private static final long serialVersionUID = -3110395132126676705L;
    private String reqId;
    private int code;
    private String msg;
    private T data;


    private WebResponse(String reqId) {
        this(reqId, ErrorCode.SUCCESS.code(), ErrorCode.SUCCESS.msg());
    }

    private WebResponse(String reqId, T data) {
        this(reqId);
        this.data = data;
    }

    private WebResponse(String reqId, int code, String msg) {
        this.reqId = reqId;
        this.code = code;
        this.msg = msg;
    }

    private WebResponse(String reqId, int code, String msg, T data) {
        this(reqId, code, msg);
        this.data = data;
    }

    public static <T> WebResponse<T> createSuccess(String reqId, T data) {
        return new WebResponse<>(reqId, data);
    }

    public static WebResponse<?> createFail(String reqId, String message) {
        return new WebResponse<>(reqId, ErrorCode.INVOKE_FAILED.code(), message, null);
    }

    public static WebResponse<?> create(String reqId, ErrorCode code) {
        return new WebResponse<>(reqId, code.code(), code.msg());
    }

    public static WebResponse<?> create(String reqId, ErrorCode code, String msg) {
        return new WebResponse<>(reqId, code.code(), StringUtils.hasText(msg) ? msg : code.msg());
    }

    public static WebResponse<?> create(String reqId, int code, String msg) {
        return new WebResponse<>(reqId, code, msg);
    }

    public boolean isSuccess() {
        return ErrorCode.SUCCESS.code() == this.code;
    }
}
