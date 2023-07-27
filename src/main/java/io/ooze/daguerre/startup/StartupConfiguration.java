package io.ooze.daguerre.startup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * startup configuration
 *
 * @author zhangjw
 * @date 2022/4/2 0002 20:25
 */
@Configuration
public class StartupConfiguration {
    @Bean
    public ObjectMapper objectMapper(JacksonProperties props) {
        ObjectMapper tidyMapper = new ObjectMapper();
        tidyMapper.enable(SerializationFeature.CLOSE_CLOSEABLE)
                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                        DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .setDateFormat(new SimpleDateFormat(props.getDateFormat() == null ? "yyyy-MM-dd HH:mm:ss" :
                        props.getDateFormat()))
                .setTimeZone(props.getTimeZone() == null ? TimeZone.getTimeZone("GMT+8") :
                        props.getTimeZone())
                .setLocale(Locale.CHINESE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return tidyMapper;
    }

    @Bean("taskExecutor")
    @Lazy
    public ThreadPoolTaskExecutor executorPool() {
        int size = Runtime.getRuntime().availableProcessors() * 2;
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(size);
        pool.setMaxPoolSize(size);
        pool.setKeepAliveSeconds(60);
        pool.setQueueCapacity(1000);
        pool.setAllowCoreThreadTimeOut(true);
        pool.setThreadNamePrefix("daguerre-task-");
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return pool;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(){
        MappingJackson2HttpMessageConverter jacksonConverter = new
                MappingJackson2HttpMessageConverter();
        jacksonConverter.setSupportedMediaTypes
                (Collections.singletonList(MediaType.ALL));
        return jacksonConverter;
    }
}
