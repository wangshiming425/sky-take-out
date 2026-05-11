package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {
    Result insert(DishDTO dto);


    Result<PageResult> pagequery(DishPageQueryDTO dto);

    Result<DishVO> selectById(String id);

    Result changInfo(DishDTO dto);

    Result deleteByIds(String ids);

    Result<List<Dish>> queryByCategoryId(String categoryId);

    Result changStatus(String status,String id);
}
