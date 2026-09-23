package com.zgmall.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zgmall.api.dto.OrderDetailDTO;
import com.zgmall.common.domain.PageDTO;
import com.zgmall.item.domain.po.Item;
import com.zgmall.item.domain.query.ItemPageQuery;
import com.zgmall.item.domain.vo.ItemVO;
import com.zgmall.item.mapper.ItemMapper;
import com.zgmall.item.service.IItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    @Override
    public PageDTO<ItemVO> queryItemPage(ItemPageQuery query) {
        // TODO 核心业务待用户实现：只查上架商品 -> keyword 匹配（一期 LIKE，ES 加分项）-> categoryId 过滤
        //      -> sort 排序（sales/priceAsc/priceDesc）-> join 分类/品牌映射 categoryName/brand/desc
        throw new UnsupportedOperationException("TODO: 商品分页查询待实现");
    }

    @Override
    public ItemVO queryItemById(Long id) {
        // TODO 核心业务待用户实现：join 分类/品牌映射 VO；不存在抛 BizException(404)
        throw new UnsupportedOperationException("TODO: 商品详情待实现");
    }

    @Override
    public void deductStock(List<OrderDetailDTO> details) {
        // TODO 核心业务待用户实现：逐条条件更新 UPDATE stock = stock - num WHERE id = ? AND stock >= num
        //      影响行数 0 即库存不足，抛 BizException；注意先全部校验或事务内回滚已扣部分
        throw new UnsupportedOperationException("TODO: 库存预扣待实现");
    }

    @Override
    public void restoreStock(List<OrderDetailDTO> details) {
        // TODO 核心业务待用户实现：逐条加回库存 UPDATE stock = stock + num WHERE id = ?
        throw new UnsupportedOperationException("TODO: 库存回滚待实现");
    }
}
