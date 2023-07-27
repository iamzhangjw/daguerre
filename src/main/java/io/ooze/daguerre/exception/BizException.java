package io.ooze.daguerre.exception;

/**
 * 业务异常
 *
 * @date 2021/07/28 0028 11:18
 * @author zhangjw
 */
public class BizException extends BaseException {
    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.msg());
        this.code = errorCode.code();
    }

    public BizException(ErrorCode errorCode, String msg) {
        super(msg);
        this.code = errorCode.code();
    }

    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.msg(), cause);
        this.code = errorCode.code();
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BizException(int code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return code + " cause " + super.getMessage();
    }
}
