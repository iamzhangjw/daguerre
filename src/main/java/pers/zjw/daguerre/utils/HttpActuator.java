package pers.zjw.daguerre.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import pers.zjw.daguerre.enhance.HttpSimpleInterceptor;
import pers.zjw.daguerre.exception.HttpCallException;
import pers.zjw.daguerre.exception.UnsupportedClassException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * http call utils
 *
 * @author zhangjw
 * @date 2022/3/27 0027 14:54
 */
@Slf4j
public class HttpActuator {
    public static final List<String> ALLOW_PRINT_CONTENT_TYPE = Arrays.asList(
            MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_JSON_VALUE);

    public static final HttpActuator INSTANCE = new HttpActuator();

    private final ConcurrentHashMap<String, OkHttpClient> clientMap;
    private final OkHttpClient defaultClient;

    private HttpActuator () {
        defaultClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .addInterceptor(new HttpSimpleInterceptor())
                .build();
        clientMap = new ConcurrentHashMap<>();
    }

    public OkHttpClient client() {
        return defaultClient;
    }

    public OkHttpClient client(MediaType mediaType) {
        OkHttpClient client = clientMap.get(mediaType.toString());
        if (null != client) return client;
        synchronized (INSTANCE) {
            client = clientMap.putIfAbsent(mediaType.toString(),
                    new OkHttpClient.Builder().addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        Request requestWithUserAgent = originalRequest
                                .newBuilder()
                                .header("Content-Type", mediaType.toString())
                                .build();

                        return chain.proceed(requestWithUserAgent);
                    }).connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.MINUTES)
                            .writeTimeout(30, TimeUnit.MINUTES).build());
        }
        return client;
    }

    public <T> T get(String url, TypeReference<T> typeRef) {
        return get(url, null, typeRef);
    }

    public <T> T get(String url, Map<String, Object> params, TypeReference<T> typeRef) {
        return get(url, params, null, null, typeRef);
    }

    public <T> T get(String url, Class<T> clazz) {
        return get(url, null, clazz);
    }

    public <T> T get(String url, Map<String, Object> params, Class<T> clazz) {
        return get(url, params, null, clazz, null);
    }

    public <T> T get(String url, Map<String, Object> params, HttpHeaders headers,
                     Class<T> clazz, TypeReference<T> typeRef) {
        Assert.hasText(url, "url must not be null");
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        if (!CollectionUtils.isEmpty(params)) {
            params.forEach((k,v) -> urlBuilder.addQueryParameter(k, v.toString()));
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build().toString());
        MediaType mediaType = null;
        if (!CollectionUtils.isEmpty(headers)) {
            mediaType = headers.getContentType();
            headers.forEach((k,v) -> {
                if (!HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(k)) {
                    requestBuilder.addHeader(k, StringUtils.join(v, ","));
                }
            });
        }
        Request request = requestBuilder.build();
        OkHttpClient client = (null != mediaType) ? client(mediaType) : client();
        return parseResponse(execute(client, request), request, clazz, typeRef);
    }

    public <T> T post(String url, Object body, TypeReference<T> typeRef) {
        return post(url, body, typeRef);
    }

    public <T> T post(String url, Map<String, Object> params, Object body, TypeReference<T> typeRef) {
        return post(url, params, null, body, null, typeRef);
    }

    public <T> T post(String url, Object body, Class<T> clazz) {
        return post(url, body, clazz);
    }

    public <T> T post(String url, Map<String, Object> params, Object body, Class<T> clazz) {
        return post(url, params, null, body, clazz, null);
    }

    public <T> T post(String url, Map<String, Object> params, HttpHeaders headers,
                      Object body, Class<T> clazz, TypeReference<T> typeRef) {
        Assert.hasText(url, "url must not be null");
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        if (!CollectionUtils.isEmpty(params)) {
            params.forEach((k,v) -> urlBuilder.addQueryParameter(k, v.toString()));
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build().toString());
        MediaType mediaType = null;
        if (!CollectionUtils.isEmpty(headers)) {
            mediaType = headers.getContentType();
            headers.forEach((k,v) -> {
                if (!HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(k)) {
                    requestBuilder.addHeader(k, StringUtils.join(v, ","));
                }
            });
        }
        RequestBody requestBody = null;
        if (null != body) {
            if (null == mediaType || MediaType.MULTIPART_FORM_DATA == mediaType) {
                if (ClassUtils.isPrimitiveOrWrapper(body.getClass())
                        || ClassUtils.isPrimitiveArray(body.getClass())
                        || body instanceof Collection || body instanceof String) {
                    throw new UnsupportedClassException("不支持的类：" + body.getClass());
                }
                Map<?, ?> map = (body instanceof Map)
                        ? (Map<?, ?>) body : JsonParser.customize().convertValue(body, Map.class);
                FormBody.Builder bodyBuilder = new FormBody.Builder();
                map.forEach((k,v) -> bodyBuilder.add(k.toString(), v.toString()));
                requestBody = bodyBuilder.build();
            } else {
                String bodyString = (body instanceof String) ? (String) body : JsonParser.toString(body);
                requestBody = RequestBody.create(bodyString, okhttp3.MediaType.parse(mediaType.toString()));
            }
        }

        Request request = requestBuilder.post(requestBody).build();
        OkHttpClient client = (null != mediaType) ? client(mediaType) : client();
        return parseResponse(execute(client, request), request, clazz, typeRef);
    }

    public <T> T exchange(Request request, MediaType mediaType, Class<T> clazz) {
        return exchange(request, mediaType, clazz, null);
    }

    public <T> T exchange(Request request, MediaType mediaType, TypeReference<T> typeRef) {
        return exchange(request, mediaType, null, typeRef);
    }

    public <T> T exchange(Request request, MediaType mediaType, Class<T> clazz, TypeReference<T> typeRef) {
        OkHttpClient client = (null != mediaType) ? client(mediaType) : client();
        return parseResponse(execute(client, request), request, clazz, typeRef);
    }

    private Response execute(OkHttpClient client, Request request) {
        Call call = client.newCall(request);
        try {
            return call.execute();
        } catch (IOException e) {
            throw new HttpCallException("execute http request failed",
                    request.url().toString(), request.method(), e);
        }
    }

    private <T> T parseResponse(Response response, Request request, Class<T> clazz, TypeReference<T> typeRef) {
        HttpStatus status = HttpStatus.valueOf(response.code());
        if (status.is4xxClientError() || status.is5xxServerError()) {
            throw new HttpCallException("parse http request response failed",
                    request.url().toString(), request.method());
        }
        if (null == clazz && null == typeRef) return null;
        return (null != clazz) ? JsonParser.toObject(Objects.requireNonNull(response.body()).byteStream(), clazz)
                : JsonParser.toObject(Objects.requireNonNull(response.body()).byteStream(), typeRef);
    }
}
