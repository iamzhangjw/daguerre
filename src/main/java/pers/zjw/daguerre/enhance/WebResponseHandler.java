package pers.zjw.daguerre.enhance;

import com.fasterxml.jackson.databind.util.JSONPObject;
import pers.zjw.daguerre.exception.BizException;
import pers.zjw.daguerre.exception.ErrorCode;
import pers.zjw.daguerre.pojo.WebResponse;
import pers.zjw.daguerre.utils.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.MethodParameter;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.io.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 全局web响应及异常处理
 *
 * @date 2022/12/31 0031 14:31
 * @author zhangjw
 */
@Slf4j
@RestControllerAdvice(basePackages = {"io.ooze.daguerre"})
@ConditionalOnClass({HttpServletRequest.class})
public class WebResponseHandler implements ResponseBodyAdvice<Object> {

    @Value("${server.servlet.context-path:/daguerre}")
    private String contextPath;


    /**
     * 处理数据校验异常，包括：
     * 1.Validation 检验错误
     * 2.spring 对于 Servlet 的校验错误
     *
     * @param ex 异常对象
     * @return 错误返回数据结构
     */
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class, ServletException.class,
            ValidationException.class, IllegalArgumentException.class})
    public WebResponse<?> handleInValidEx(Exception ex, HttpServletRequest request) {
        String msg = null;
        List<ObjectError> errors = null;
        if (ex instanceof BindException) {
            errors = ((BindException) ex).getAllErrors();
        }
        if (ex instanceof MethodArgumentNotValidException) {
            errors = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
        }
        if (!CollectionUtils.isEmpty(errors)) {
            msg = resolveMsgFromError(errors.get(0));
        }
        msg = !StringUtils.hasText(msg) ? ex.getMessage() : msg;

        return WebResponse.create(getReqId(request), ErrorCode.MISMATCH_PARAM, msg);
    }

    /**
     * 从 ObjectError 之中获取错误信息
     */
    private String resolveMsgFromError(ObjectError objectError) {
        if (!(objectError instanceof FieldError)) {
            return objectError.getDefaultMessage();
        }
        FieldError fieldError = (FieldError) objectError;
        return fieldError.getField() + fieldError.getDefaultMessage();
    }

    /**
     * 处理 spring 运行时异常
     */
    @ExceptionHandler(NestedRuntimeException.class)
    public WebResponse<?> handleSpringRuntimeEx(NestedRuntimeException ex, HttpServletRequest request) {
        logError(ex, request);
        String className = ex.getClass().getName();
        String msg = null;
        // spring web 和 http 包中的从异常之中获取信息，其余的返回默认错误信息
        if (className.startsWith("org.springframework.web")
                || className.startsWith("org.springframework.http")) {
            msg = ex.getMessage();
        }
        msg = !StringUtils.hasText(msg) ? ErrorCode.INVOKE_FAILED.msg() : msg;
        return WebResponse.create(getReqId(request), ErrorCode.INVOKE_FAILED, msg);
    }

    /**
     * 重复键值异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public WebResponse<?> handleDuplicateKeyEx(DuplicateKeyException ex, HttpServletRequest request) {
        logError(ex, request);
        String msg = ErrorCode.INVOKE_FAILED.msg();
        String errorMsg = ex.getCause().getMessage();
        if (StringUtils.hasText(errorMsg)) {
            String[] array = errorMsg.split("'");
            if (array.length >= 4) {
                String paramValue = array[1];
                String paramName = array[3];
                msg = "parameter[" + paramName + "]'s value '" + paramValue + "' exists";
            }
        }
        return WebResponse.createFail(getReqId(request), msg);
    }

    /**
     * 处理客户端主动关闭连接的异常
     */
    @ExceptionHandler({ClientAbortException.class})
    public WebResponse<?> handleClientClosedEx(ClientAbortException e, HttpServletRequest request) {
        log.info("client close connection, url {}, ip {}, msg {}",
                request.getRequestURI(), request.getRemoteUser(), e.getMessage());
        return WebResponse.createFail(getReqId(request), "client close request");
    }

    /**
     * 处理自定义 code 和 msg 的异常
     */
    @ExceptionHandler({BizException.class})
    public WebResponse<?> handleBizEx(BizException e, HttpServletRequest request) {
        logError(e, e.getMessage(), request);
        return WebResponse.create(getReqId(request), e.getCode(), e.getMessage());
    }

    @ExceptionHandler({Exception.class})
    public WebResponse<?> handleEx(Exception e, HttpServletRequest request) {
        logError(e, request);
        return WebResponse.create(getReqId(request), ErrorCode.INVOKE_FAILED, e.getMessage());
    }

    private void logError(Throwable throwable, HttpServletRequest request) {
        logError(throwable, null, request);
    }

    private void logError(Throwable throwable, String fullMsg, HttpServletRequest request) {
        try {
            log.warn("call controller occurred exception: {} {}, request id {}, request mapping {}, param: {}, body: {}",
                    throwable.getClass().getName(), StringUtils.hasText(fullMsg) ? fullMsg : throwable.getMessage(),
                    request.getHeader("id"), request.getHeader("uri"),
                    JsonParser.toString(request.getParameterMap()),
                    request.getReader().lines().collect(Collectors.joining("")),
                    throwable);
        } catch (IOException e) {
            log.error("get controller's body occurred exception, request id {}, request mapping {}, param: {}",
                    request.getHeader("id"), request.getRequestURI(),
                    JsonParser.toString(request.getParameterMap()), e);
        }
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body, @NotNull MethodParameter returnType, @NotNull MediaType mediaType,
            @NotNull Class<? extends HttpMessageConverter<?>> converterType,
            ServerHttpRequest httpRequest, @NotNull ServerHttpResponse response) {
        HttpServletRequest request = ((ServletServerHttpRequest) httpRequest).getServletRequest();
        boolean ignore = false;
        if ((contextPath + "/error").equalsIgnoreCase(httpRequest.getURI().getPath())) {
            body = WebResponse.create(getReqId(request), ErrorCode.INVOKE_FAILED);
            response.getHeaders().add("error", "error");
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        } else if (body instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) body;
            Object status = map.get("status");
            if (Objects.nonNull(status) && isError(HttpStatus.valueOf((Integer) status))) {
                body = WebResponse.createFail(getReqId(request), (String) map.get("message"));
                response.getHeaders().add("error", "error");
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            }
        }
        //本身已经是 WebResponse 对象则不再包装，说明是异常处理后的返回
        if (body instanceof WebResponse) {
            WebResponse<?> webResponse = (WebResponse<?>) body;
            if (!webResponse.isSuccess()) {
                response.getHeaders().add("error", "error");
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            }
        } else if (body instanceof Resource) {
            ignore = true;
        } else {
            body = WebResponse.createSuccess(getReqId(request), body);
        }
        //jsonp 支持
        String callBackFunction = request.getParameter("callback");
        if (!ignore) {
            if (StringHttpMessageConverter.class.equals(converterType)) {
                // string转换器修改 content-type
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                body = JsonParser.toString(body);
                // 字符串类型要自己拼接
                if (StringUtils.hasText(callBackFunction)) {
                    body = callBackFunction + "(" + body + ")";
                }
            } else if (StringUtils.hasText(callBackFunction)) {
                body = new JSONPObject(callBackFunction, body);
            }
        }

        if ("/heartbeat".equals(request.getServletPath())) {
            return body;
        }

        StringBuilder requestBody = new StringBuilder();
        try {
            request.getReader().lines().forEach(requestBody::append);
        } catch (Exception e) {
            log.error("get request occurred exception: {} {}", e.getClass().getName(), e.getMessage());
        }
        long cost = System.currentTimeMillis() - Long.parseLong(request.getHeader("start"));
        log.info("call controller mapping request id: {}, url: {} with {} {} from {}, request body: {}, response body: {}, cost {}ms.",
                request.getHeader("id"), request.getRequestURI(), JsonParser.toString(request.getParameterMap()),
                httpRequest.getMethod(), request.getRemoteUser(), stringifyRequestBody(request),
                stringifyResponseBody(mediaType, body), cost);
        return body;
    }

    private boolean isError(HttpStatus status) {
        return (status.is4xxClientError() || status.is5xxServerError());
    }


    private String getReqId(HttpServletRequest request) {
        return request.getHeader("id");
    }

    private String stringifyRequestBody(HttpServletRequest request) {
        if (MediaType.MULTIPART_FORM_DATA_VALUE.equalsIgnoreCase(request.getContentType())) {
            return null;
        }
        StringBuilder requestBody = new StringBuilder();
        try {
            request.getReader().lines().forEach(requestBody::append);
        } catch (Exception e) {
            log.error("get request occurred exception: {} {}", e.getClass().getName(), e.getMessage());
        }
        return requestBody.toString();
    }

    private String stringifyResponseBody(MediaType mediaType, Object body) {
        if (!MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(mediaType.toString())) {
            return null;
        }
        if (body instanceof Stream || body instanceof Resource) return null;
        return (body instanceof String) ? (String) body : JsonParser.toString(body);
    }
}
