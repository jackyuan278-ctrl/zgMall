package com.zgmall.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zgmall.item.domain.po.Category;
import com.zgmall.item.domain.vo.CategoryVO;
import com.zgmall.item.mapper.CategoryMapper;
import com.zgmall.item.service.ICategoryService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Override
    public List<CategoryVO> listCategories() {
        List<Category> list = lambdaQuery().orderByAsc(Category::getSort).list();
        List<CategoryVO> categoryVOList = new ArrayList<>();
        for (Category category : list) {
            CategoryVO categoryVO = BeanUtil.copyProperties(category, CategoryVO.class);
            categoryVOList.add(categoryVO);
        }
        return categoryVOList;
    }
}
