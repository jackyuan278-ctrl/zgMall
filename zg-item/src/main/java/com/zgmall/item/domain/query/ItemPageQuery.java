package com.zgmall.item.domain.query;

import com.zgmall.common.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分页查询参数，字段名与前端契约一致
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ItemPageQuery extends PageQuery {

    private String keyword;

    private Long categoryId;

    /** '' 综合 / sales 销量 / priceAsc 价格升序 / priceDesc 价格降序 */
    private String sort;
}
