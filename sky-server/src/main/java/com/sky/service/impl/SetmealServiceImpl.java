package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐
     *
     * @param dto
     * @return
     */
    @Transactional
    @AutoFill(OperationType.INSERT)
    @Override
    public Result insert(SetmealDTO dto) {
        //获取新增的菜品
        List<SetmealDish> setmealDishes = dto.getSetmealDishes();
        //遍历每个菜品去查询是否起售
        for (SetmealDish setmealDish : setmealDishes) {
            Long dishId = setmealDish.getDishId();
            Dish dish = dishMapper.selectOne(new LambdaQueryWrapper<Dish>().eq(Dish::getId, dishId));
            if (dish == null || dish.getStatus() == StatusConstant.DISABLE) {
                return Result.error(MessageConstant.SETMEAL_HAVE_UNABLE_DISH);
            }

        }
        Setmeal newSetmeal = new Setmeal();
        BeanUtils.copyProperties(dto, newSetmeal);
        //刚添加的套餐设为未起售,默认值
        /*newSetmeal.setStatus(StatusConstant.ENABLE);*/
        setmealMapper.insert(newSetmeal);
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(newSetmeal.getId());
        }

        setmealDishMapper.insert(setmealDishes);
        return Result.success();
    }

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public Result<PageResult> pageQuery(SetmealPageQueryDTO dto) {
        int page = dto.getPage();
        Integer status = dto.getStatus();
        String name = dto.getName();
        int pageSize = dto.getPageSize();
        Integer categoryId = dto.getCategoryId();
        //开始分页查询
        Page<Setmeal> queryPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Strings.isNotBlank(name), Setmeal::getName, name).eq(status != null, Setmeal::getStatus, status)
                .eq(categoryId != null, Setmeal::getCategoryId, categoryId);
        Page<Setmeal> setmealPage = setmealMapper.selectPage(queryPage, queryWrapper);
        PageResult pageResult = PageResult.builder().total(setmealPage.getTotal()).records(setmealPage.getRecords()).build();
        return Result.success(pageResult);

    }

    /**
     * 查询单个套餐具体信息
     *
     * @param id
     * @return
     */
    @Override
    public Result<SetmealVO> selectById(String id) {
        //先区套餐表获取一些基本信息
        Setmeal setmeal = setmealMapper.selectById(id);
        if (setmeal == null) {
            return Result.error(MessageConstant.SETMEAL_NOT_EXISTS);
        }
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        //根据套餐id去套餐菜品表查询
        List<SetmealDish> setmealDishes = setmealDishMapper.
                selectList(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));
        setmealVO.setSetmealDishes(setmealDishes);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     *
     * @param dto
     * @return
     */
    @Transactional
    @AutoFill(OperationType.UPDATE)
    @Override
    public Result changIndo(SetmealDTO dto) {
        if (dto == null) {
            return Result.error(MessageConstant.SETMEAL_CHANGE_TRANSFROM_FAILED);
        }
        long setmealId = dto.getId();
        List<SetmealDish> newSetmealDishs = dto.getSetmealDishes();
        for (SetmealDish newSetmealDish : newSetmealDishs) {
            newSetmealDish.setSetmealId(setmealId);
        }
        //先修改套餐信息
        Setmeal newSetmeal = new Setmeal();
        BeanUtils.copyProperties(dto, newSetmeal);
        setmealMapper.updateById(newSetmeal);
        //再去修改套餐菜品信息(先删除,后添加)
        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, setmealId));
        setmealDishMapper.insert(newSetmealDishs);
        return Result.success();
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @Transactional
    @Override
    public Result delete(String ids) {
        //先获取要删除的id集合
        List<String> idlist = Arrays.stream(ids.split(",")).toList();
        //先去删除套餐,起售的套餐不可以删除
        for (String id : idlist) {
            Setmeal setmeal = setmealMapper.selectOne(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getId, id));
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                return Result.error(MessageConstant.SETMEAL_ON_SALE);
            }
            setmealMapper.deleteById(id);
            setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));
        }
        return Result.success();
    }

    /**
     * 修改套餐状态
     *
     * @param status
     * @param id
     * @return
     */
    @Transactional
    @Override
    public Result changstatus(String status, String id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        if (setmeal == null) {
            return Result.error(MessageConstant.SETMEAL_NOT_EXISTS);
        }
        setmeal.setStatus(Integer.valueOf(status));
        // 手动设置更新时间和更新人
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealMapper.updateById(setmeal);
        return Result.success();
    }

}
