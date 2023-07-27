package io.ooze.daguerre.enhance;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局接口
 *
 * @date 2021/08/06 0006 14:51
 * @author zhangjw
 */
@RestController
public class GlobalController {

    /**
     * 全局异常处置
     * @param request http servlet request
     */
    @RequestMapping("/error/exception")
    public void exception(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 抛出 filter 转发过来的异常。让 ControllerAdvice 处理
        throw ((RuntimeException) request.getAttribute("filter.error"));
    }

    /**
     * 心跳
     */
    @RequestMapping("/heartbeat")
    public void heartbeat() {
        return;
    }
}
