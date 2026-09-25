package com.zgmall.common.config;

import com.zgmall.common.interceptor.UserContext;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 身份接力：把当前线程 user-info 头透传给下游服务（依赖 openfeign，未引入时整段跳过）
 */
@Configuration
@ConditionalOnClass(RequestInterceptor.class)
public class FeignRelayConfig {

    @Bean
    public RequestInterceptor userInfoFeignInterceptor() {
        return template -> {
            Long userId = UserContext.getUser();
            if (userId != null) {
                template.header("user-info", userId.toString());
            }
        };
    }
}
