package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;

import java.util.List;

public interface CategoryService extends IService<Category> {
    Result<PageResult> pagequery(CategoryPageQueryDTO dto);

    Result delete(Integer id);

    Result insert(CategoryDTO categoryDTO);

    Result changInFo(CategoryDTO categoryDTO);

    Result changStatus(Integer status, String id);

    Result<List<Category>> selectByType(String type);
}
