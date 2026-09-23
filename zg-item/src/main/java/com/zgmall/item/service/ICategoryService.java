package com.zgmall.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zgmall.item.domain.po.Category;
import com.zgmall.item.domain.vo.CategoryVO;

import java.util.List;

public interface ICategoryService extends IService<Category> {

    List<CategoryVO> listCategories();
}
