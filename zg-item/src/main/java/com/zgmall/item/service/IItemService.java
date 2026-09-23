package com.zgmall.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zgmall.api.dto.OrderDetailDTO;
import com.zgmall.common.domain.PageDTO;
import com.zgmall.item.domain.po.Item;
import com.zgmall.item.domain.query.ItemPageQuery;
import com.zgmall.item.domain.vo.ItemVO;

import java.util.List;

public interface IItemService extends IService<Item> {

    PageDTO<ItemVO> queryItemPage(ItemPageQuery query);

    ItemVO queryItemById(Long id);

    /** 下单预扣库存（Feign 直连），条件更新防超卖 */
    void deductStock(List<OrderDetailDTO> details);

    /** 超时关单回滚库存（Feign 直连） */
    void restoreStock(List<OrderDetailDTO> details);
}
