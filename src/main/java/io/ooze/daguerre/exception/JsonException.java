package io.ooze.daguerre.exception;

/**
 * json exception
 *
 * @author zhangjw
 * @date 2022/03/30 0030 18:30
 */
public class JsonException extends BaseException {
    public JsonException() {
        super();
    }

    public JsonException(String s) {
        super(s);
    }

    public JsonException(String s, Throwable t) {
        super(s, t);
    }
}
