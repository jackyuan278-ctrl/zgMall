package com.zgmall.api.client;

import com.zgmall.api.dto.ItemDTO;
import com.zgmall.api.dto.OrderDetailDTO;
import com.zgmall.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "item-service", path = "/items")
public interface ItemClient {

    @GetMapping("/{id}")
    Result<ItemDTO> queryItemById(@PathVariable("id") Long id);

    @PutMapping("/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> details);

    @PutMapping("/stock/restore")
    void restoreStock(@RequestBody List<OrderDetailDTO> details);
}
