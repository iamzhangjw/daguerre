package io.ooze.daguerre.exception;

import io.ooze.daguerre.DaguerreApplication;
import org.apache.commons.lang3.ArrayUtils;

/**
 * base exception
 *
 * @author zhangjw
 * @date 2022/03/30 0030 14:51
 */
public class BaseException extends RuntimeException {
    private final String packagePath = DaguerreApplication.class.getPackage().getName();

    private Throwable cause;

    public BaseException() {
        super();
    }

    public BaseException(String s) {
        super(s);
    }

    public BaseException(String s, Throwable t) {
        super(s, t);
        this.cause = t;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + extractCauseMsg();
    }

    private String extractCauseMsg() {
        StackTraceElement[] elements = getStackTrace();
        if (null != cause) {
            elements = cause.getStackTrace();
        }
        if (ArrayUtils.isEmpty(elements)) return "";
        for (StackTraceElement element : elements) {
            if (element.getClassName().startsWith(packagePath)) {
                return extractTraceMsg(element);
            }
        }
        return extractTraceMsg(elements[0]);
    }

    private String extractTraceMsg(StackTraceElement element) {
        return " at " + element.getClassName() + "." + element.getMethodName() + ":" + element.getLineNumber();
    }
}
