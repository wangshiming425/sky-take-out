package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.SetmealVO;

public interface SetmealService extends IService<Setmeal> {
    Result insert(SetmealDTO dto);

    Result<PageResult> pageQuery(SetmealPageQueryDTO dto);

    Result<SetmealVO> selectById(String id);

    Result changIndo(SetmealDTO dto);

    Result delete(String ids);

    Result changstatus(String status, String id);
}
