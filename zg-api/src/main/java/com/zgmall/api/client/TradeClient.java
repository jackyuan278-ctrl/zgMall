package com.zgmall.api.client;

import com.zgmall.api.dto.OrderDTO;
import com.zgmall.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 交易服务客户端（支付服务查单核价用，Feign 直连不经网关）
 */
@FeignClient(value = "trade-service", path = "/orders")
public interface TradeClient {

    @GetMapping("/{id}")
    Result<OrderDTO> queryOrderById(@PathVariable("id") Long id);
}
