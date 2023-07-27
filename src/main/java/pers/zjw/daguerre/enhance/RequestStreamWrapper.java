package pers.zjw.daguerre.enhance;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HttpServletRequest input stream 包装
 * 避免 stream 只能读取一次
 *
 * @date 2021/08/04 0004 09:12
 * @author zhangjw
 */
public class RequestStreamWrapper extends HttpServletRequestWrapper {
    private String charset;
    private byte[] cachedBytes;
    private final Map<String, String> customHeaders;


    public RequestStreamWrapper(HttpServletRequest request) {
        super(request);
        this.customHeaders = new HashMap<>();
        putHeader("start", String.valueOf(System.currentTimeMillis()));
        putHeader("id", UUID.randomUUID().toString());
        putHeader("uri", request.getRequestURI());
    }

    public RequestStreamWrapper(HttpServletRequest request, String charset) {
        this(request);
        this.charset = charset;
    }


    @Override
    public String getCharacterEncoding() {
        if (charset != null) {
            return charset;
        }
        String c = getRequest().getCharacterEncoding();
        return StringUtils.isBlank(c) ? StandardCharsets.UTF_8.displayName() : c;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBytes == null) {
            cacheInputStream();
        }
        return new ByteArrayServletInputStream(cachedBytes);
    }

    private void cacheInputStream() throws IOException {
        cachedBytes = StreamUtils.copyToByteArray(super.getInputStream());
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(
                            getInputStream(), getCharacterEncoding()));
    }

    public void putHeader(String name, String value){
        this.customHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        // check the custom headers first
        String headerValue = customHeaders.get(name);

        if (headerValue != null){
            return headerValue;
        }
        // else return from into the original wrapped object
        return ((HttpServletRequest) getRequest()).getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        // create a set of the custom header names
        Set<String> set = new HashSet<String>(customHeaders.keySet());

        // now add the headers from the wrapped request object
        Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (e.hasMoreElements()) {
            // add the names of the request headers into the list
            String n = e.nextElement();
            set.add(n);
        }

        // create an enumeration from the set and return
        return Collections.enumeration(set);
    }

    private static class ByteArrayServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        private ByteArrayServletInputStream(byte[] bytes) {
            Assert.notNull(bytes, "bytes must not be null");
            this.inputStream = new ByteArrayInputStream(bytes);
        }

        @Override
        public boolean isFinished() {
            return 0 == inputStream.available();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
        }

        @Override
        public int read() {
            return inputStream.read();
        }

    }
}
