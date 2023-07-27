package pers.zjw.daguerre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * main class
 *
 * @author zhangjw
 * @date 2022/3/25 0025 13:57
 */
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
@ComponentScan("pers.zjw.daguerre")
@ServletComponentScan
@SpringBootApplication
public class DaguerreApplication {
    public static void main(String[] args) {
        SpringApplication.run(DaguerreApplication.class, args);
    }
}
