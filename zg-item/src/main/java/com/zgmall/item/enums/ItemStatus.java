package com.zgmall.item.enums;

import com.zgmall.common.BizException;
import lombok.Getter;

@Getter
public enum ItemStatus {

    OFF_SHELF(0, "下架"),
    ON_SHELF(1, "上架");

    private final int value;
    private final String desc;

    ItemStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static ItemStatus of(Integer value) {
        if (value == null) {
            return ON_SHELF;
        }
        for (ItemStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new BizException(400, "非法的商品状态: " + value);
    }
}
