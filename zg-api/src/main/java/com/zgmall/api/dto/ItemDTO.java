package com.zgmall.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemDTO {

    private Long id;
    private String name;
    private Integer price;
    private Integer stock;
    private String image;
    private Long categoryId;
    private Long brandId;
    private String spec;
    private Integer sales;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
