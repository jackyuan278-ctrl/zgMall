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
        if (query.getCategoryId() != null && query.getCategoryId() <= 0) {
            throw new BizException(400, "商品分类不合法");
        }
        if (query.getCategoryId() != null) {
            lq.eq(Item::getCategoryId, query.getCategoryId());
        }

        boolean cursorMode = query.getCursorId() != null;
        if (query.getPage() != null && query.getPage() > 5 && !cursorMode) {
            throw new BizException(400, "第6页起请使用游标分页");
        }
        if (cursorMode && query.getCursorId() <= 0) {
            throw new BizException(400, "分页游标不合法");
        }
        if (cursorMode && query.getPage() != null && query.getPage() != 1) {
            throw new BizException(400, "游标分页不能同时指定页码");
        }

        String sort = StrUtil.blankToDefault(query.getSort(), "");
        switch (sort) {
            case "sales" :
                lq.orderByDesc(Item::getSales).orderByAsc(Item::getId);
                break;
            case "priceAsc" :
                lq.orderByAsc(Item::getPrice).orderByAsc(Item::getId);
                break;
            case "priceDesc" :
                lq.orderByDesc(Item::getPrice).orderByAsc(Item::getId);
                break;
            case "" :
                lq.orderByAsc(Item::getId);
                break;
            default:
                throw new BizException(400, "不支持的商品排序方式");
        }

        if (cursorMode) {
            if (sort.isEmpty()) {
                if (query.getCursorSortValue() != null) {
                    throw new BizException(400, "默认排序不需要排序游标值");
                }
            } else if (query.getCursorSortValue() == null) {
                throw new BizException(400, "当前排序缺少排序游标值");
            }
            switch (sort) {
                case "sales":
                    lq.and(w -> w.lt(Item::getSales, query.getCursorSortValue())
                            .or().eq(Item::getSales, query.getCursorSortValue()).gt(Item::getId, query.getCursorId()));
                    break;
                case "priceAsc":
                    lq.and(w -> w.gt(Item::getPrice, query.getCursorSortValue())
                            .or().eq(Item::getPrice, query.getCursorSortValue()).gt(Item::getId, query.getCursorId()));
                    break;
                case "priceDesc":
                    lq.and(w -> w.lt(Item::getPrice, query.getCursorSortValue())
                            .or().eq(Item::getPrice, query.getCursorSortValue()).gt(Item::getId, query.getCursorId()));
                    break;
                default:
                    lq.gt(Item::getId, query.getCursorId());
            }
        }

        Page<Item> page = cursorMode
                ? new Page<>(1, normalizedPageSize(query.getPageSize()) + 1L, false)
                : query.toMpPage();
        Page<Item> result = this.page(page, lq);
        boolean hasMore = cursorMode && result.getRecords().size() > normalizedPageSize(query.getPageSize());
        if (hasMore) {
            result.getRecords().remove(result.getRecords().size() - 1);
        }

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
        pageDTO.setTotal(cursorMode ? null : result.getTotal());
        pageDTO.setList(itemVoList);
        if (cursorMode) {
            pageDTO.setHasMore(hasMore);
            Item lastItem = result.getRecords().isEmpty() ? null : result.getRecords().get(result.getRecords().size() - 1);
            pageDTO.setNextCursor(lastItem == null ? null : lastItem.getId());
            pageDTO.setNextCursorSortValue(lastItem == null || sort.isEmpty() ? null
                    : "sales".equals(sort) ? lastItem.getSales() : lastItem.getPrice());
        }

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
        validateStockDetails(details);
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
        validateStockDetails(details);
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

    private void validateStockDetails(List<OrderDetailDTO> details) {
        if (details == null || details.isEmpty()) {
            throw new BizException(400, "商品明细不能为空");
        }
        for (OrderDetailDTO detail : details) {
            if (detail == null || detail.getItemId() == null) {
                throw new BizException(400, "商品信息不完整");
            }
            if (detail.getNum() == null || detail.getNum() <= 0) {
                throw new BizException(400, "商品数量必须大于0");
            }
        }
    }


    private long normalizedPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return PageQuery.DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, PageQuery.MAX_PAGE_SIZE);
    }

}
