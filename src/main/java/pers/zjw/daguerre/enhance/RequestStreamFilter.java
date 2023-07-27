package pers.zjw.daguerre.enhance;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * http 请求 stream 包装 filter
 *
 * @date 2021/08/04 0004 9:47
 * @author zhangjw
 */
public class RequestStreamFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // request 的 input stream 只能读取一次
        RequestStreamWrapper wrapper = new RequestStreamWrapper((HttpServletRequest) request);
        chain.doFilter(wrapper, response);
    }
}
