package com.zgmall.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zgmall.item.domain.po.Category;
import com.zgmall.item.domain.vo.CategoryVO;
import com.zgmall.item.mapper.CategoryMapper;
import com.zgmall.item.service.ICategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Override
    public List<CategoryVO> listCategories() {
        // TODO 核心业务待用户实现：查全部分类按 sort 升序 -> 转 VO
        throw new UnsupportedOperationException("TODO: 分类列表待实现");
    }
}
