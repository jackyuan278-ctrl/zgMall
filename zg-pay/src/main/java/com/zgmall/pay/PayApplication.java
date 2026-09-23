package com.zgmall.pay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.zgmall.pay.mapper")
@EnableFeignClients(basePackages = "com.zgmall.api.client")
@SpringBootApplication(scanBasePackages = "com.zgmall")
public class PayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }
}
