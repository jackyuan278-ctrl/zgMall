package com.zgmall.api.client;

import com.zgmall.api.dto.ItemDTO;
import com.zgmall.api.dto.OrderDetailDTO;
import com.zgmall.common.Result;
import com.zgmall.common.domain.PageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "item-service", path = "/items")
public interface ItemClient {

    @GetMapping("/{id}")
    Result<ItemDTO> queryItemById(@PathVariable("id") Long id);

    /** 分页查商品（zg-ai 知识入库批量拉全量用，pageSize 上限 50） */
    @GetMapping
    Result<PageDTO<ItemDTO>> queryItemPage(@RequestParam("page") Integer page,
                                           @RequestParam("pageSize") Integer pageSize);

    @PutMapping("/stock/deduct")
    Result<Void> deductStock(@RequestBody List<OrderDetailDTO> details);

    @PutMapping("/stock/restore")
    Result<Void> restoreStock(@RequestBody List<OrderDetailDTO> details);
}
