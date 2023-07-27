package pers.zjw.daguerre.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * demo controller
 *
 * @author zhangjw
 * @date 2022/05/21 14:33
 */
@RequestMapping
@RestController
public class DemoController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/hello/ex")
    public String helloEx() {
        throw new RuntimeException("runtime exception");
    }
}
