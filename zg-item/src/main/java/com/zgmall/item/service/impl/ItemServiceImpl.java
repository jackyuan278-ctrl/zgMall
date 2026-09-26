package com.zgmall.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zgmall.api.dto.OrderDetailDTO;
import com.zgmall.common.BizException;
import com.zgmall.common.domain.PageDTO;
import com.zgmall.common.domain.PageQuery;
import com.zgmall.item.domain.po.Brand;
import com.zgmall.item.domain.po.Category;
import com.zgmall.item.domain.po.Item;
import com.zgmall.item.domain.query.ItemPageQuery;
import com.zgmall.item.domain.vo.ItemVO;
import com.zgmall.item.enums.ItemStatus;
import com.zgmall.item.mapper.BrandMapper;
import com.zgmall.item.mapper.CategoryMapper;
import com.zgmall.item.mapper.ItemMapper;
import com.zgmall.item.service.IItemService;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {
    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;
    @Override
    public PageDTO<ItemVO> queryItemPage(ItemPageQuery query) {
        LambdaQueryWrapper<Item> lq = new QueryWrapper<Item>().lambda();
        lq.eq(Item::getStatus, ItemStatus.ON_SHELF.getValue());
        if (StrUtil.isNotBlank(query.getKeyword())) {
            lq.like(Item::getName, query.getKeyword());
        }
        if (query.getCategoryId() != null) {
            lq.eq(Item::getCategoryId, query.getCategoryId());
        }

        if (StrUtil.isNotBlank(query.getSort())) {
            switch (query.getSort()) {
                case "sales" : lq.orderByDesc(Item::getSales); break;
                case "priceAsc" : lq.orderByAsc(Item::getPrice); break;
                case "priceDesc" : lq.orderByDesc(Item::getPrice); break;
                default:  break;
            }
        } else {
            // 无排序时按主键稳定翻页：MySQL 无 ORDER BY 的 LIMIT 顺序不保证，AI 入库逐页拉取会漏商品
            lq.orderByAsc(Item::getId);
        }
        Page<Item> result = this.page(query.toMpPage(), lq);

        List<Long> categoryIds = result.getRecords().stream()
                .map(Item::getCategoryId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> brandIds = result.getRecords().stream()
                .map(Item::getBrandId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));
        Map<Long, Brand> brandMap = brandIds.isEmpty() ? Collections.emptyMap()
                : brandMapper.selectBatchIds(brandIds).stream()
                        .collect(Collectors.toMap(Brand::getId, b -> b));

        List<ItemVO> itemVoList = new ArrayList<>();
        for (Item item : result.getRecords()) {
            ItemVO itemVO = BeanUtil.copyProperties(item, ItemVO.class);
            Category category = categoryMap.get(item.getCategoryId());
            Brand brand = brandMap.get(item.getBrandId());
            if (category != null) {
                itemVO.setCategoryName(category.getName());
            }
            if (brand != null) {
                itemVO.setBrand(brand.getName());
            }
            itemVoList.add(itemVO);
        }
        PageDTO<ItemVO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(result.getTotal());
        pageDTO.setList(itemVoList);

        return pageDTO;
    }

    @Override
    public ItemVO queryItemById(Long id) {
        Item item = getById(id);
        if (item == null) {
            throw new BizException(400,"商品不存在");
        }
        ItemVO itemVO = BeanUtil.copyProperties(item, ItemVO.class);
        Long categoryId = item.getCategoryId();
        Long brandId = item.getBrandId();
        Brand brand = brandMapper.selectById(brandId);
        Category category = categoryMapper.selectById(categoryId);
        if (category != null) {
            itemVO.setCategoryName(category.getName());
        }
        if (brand != null) {
            itemVO.setBrand(brand.getName());
        }
        return itemVO;
    }

    @Override
    @Transactional(rollbackFor = BizException.class)
    public void deductStock(List<OrderDetailDTO> details) {
        details.sort(Comparator.comparing(OrderDetailDTO::getItemId));
        for (OrderDetailDTO detail : details) {
            Integer num = detail.getNum();
            Long itemId = detail.getItemId();
            boolean update = lambdaUpdate().eq(Item::getId, itemId).ge(Item::getStock, num).setDecrBy(Item::getStock, num).update();
            if (!update) {
                Item item = getById(itemId);
                if (item != null) {
                    throw new BizException(400,"商品库存不足");
                }else{
                    throw new BizException(400,"商品不存在");
                }

            }
        }
    }

    @Override
    @Transactional(rollbackFor = BizException.class)
    public void restoreStock(List<OrderDetailDTO> details) {
        for (OrderDetailDTO detail : details) {
            Integer num = detail.getNum();
            Long itemId = detail.getItemId();
            Item item = getById(itemId);
            if (item == null) {
                throw new BizException(400,"商品不存在" );
            }
            lambdaUpdate().eq(Item::getId, itemId).setIncrBy(Item::getStock,num).update();

        }
    }
}
