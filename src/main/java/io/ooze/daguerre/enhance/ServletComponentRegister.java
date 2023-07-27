package io.ooze.daguerre.enhance;

import io.ooze.daguerre.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * servlet component register, such as filter, interceptor, listener
 *
 * jdk 提供的 WebFilter 不支持加载顺序，因此需要借助 FilterRegistrationBean 的方式注入
 *
 *
 * @date 2021/10/09 0009 15:01
 * @author zhangjw
 */
@Configuration
public class ServletComponentRegister {
    @Value("${daguerre.sign.enable:false}")
    private boolean enableSign;
    @Autowired
    private CredentialService credentialService;

    @Bean
    public FilterRegistrationBean<ApiAccessibleFilter> accessibleFilter() {
        FilterRegistrationBean<ApiAccessibleFilter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(new ApiAccessibleFilter(enableSign, credentialService));
        filterRegBean.setName("accessibleFilter");
        filterRegBean.setUrlPatterns(Collections.singleton("/oss/*"));
        filterRegBean.setOrder(1);
        return filterRegBean;
    }

    @Bean
    public FilterRegistrationBean<ExceptionFilter> exceptionFilter() {
        FilterRegistrationBean<ExceptionFilter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(new ExceptionFilter());
        filterRegBean.setName("exceptionFilter");
        filterRegBean.addUrlPatterns("/*");
        filterRegBean.setOrder(0);
        return filterRegBean;
    }

    @Bean
    public FilterRegistrationBean<RequestStreamFilter> requestStreamFilter() {
        FilterRegistrationBean<RequestStreamFilter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(new RequestStreamFilter());
        filterRegBean.setName("requestStreamFilter");
        filterRegBean.addUrlPatterns("/*");
        filterRegBean.setOrder(-1);
        return filterRegBean;
    }
}
