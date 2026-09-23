package com.zgmall.trade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.zgmall.trade.mapper")
@EnableFeignClients(basePackages = "com.zgmall.api.client")
@SpringBootApplication(scanBasePackages = "com.zgmall")
public class TradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeApplication.class, args);
    }
}
