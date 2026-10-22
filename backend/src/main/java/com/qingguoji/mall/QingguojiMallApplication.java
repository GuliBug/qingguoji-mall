package com.qingguoji.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 青果纪商城后端启动类
 *
 * @author qingguoji
 */
@EnableScheduling
@SpringBootApplication
public class QingguojiMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(QingguojiMallApplication.class, args);
    }
}
