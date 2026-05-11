package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     *
     * @param dto
     * @return
     */
    @Transactional
    @AutoFill(OperationType.INSERT)
    @Override
    public Result insert(DishDTO dto) {
        Dish dish = new Dish();
        List<DishFlavor> flavors = dto.getFlavors();
        //先判断是否重名
        String name = dto.getName();
        Dish dish1 = dishMapper.selectOne(new LambdaQueryWrapper<Dish>().eq(Strings.isNotBlank(name), Dish::getName, name));
        if (dish1 != null) {
            return Result.error(MessageConstant.DISH_HAVIED_EXIST);
        }
        BeanUtils.copyProperties(dto, dish);
        dishMapper.insert(dish);
        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insert(flavors);
        }
        return Result.success();
    }

    /**
     * 菜品分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public Result<PageResult> pagequery(DishPageQueryDTO dto) {
        String name = dto.getName();
        int page = dto.getPage();
        Integer categoryId = dto.getCategoryId();
        int pageSize = dto.getPageSize();
        Integer status = dto.getStatus();
        Page<Dish> page1 = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Strings.isNotBlank(name), Dish::getName, name).eq(status != null, Dish::getStatus, status)
                .eq(categoryId != null, Dish::getCategoryId, categoryId);
        Page<Dish> resultpage = dishMapper.selectPage(page1, queryWrapper);
        PageResult pageResult = PageResult.builder().records(resultpage.getRecords()).total(resultpage.getTotal()).build();
        return Result.success(pageResult);
    }

    /**
     * 根据菜品Id查询详细信息
     *
     * @param id
     * @return
     */
    @Override
    public Result<DishVO> selectById(String id) {
        //先查询数据库是否存在该数据
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            return Result.error(MessageConstant.DISH_NOT_EXIST);
        }
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dish.getId()));
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息
     *
     * @param dto
     * @return
     */
    @Transactional
    @AutoFill(OperationType.UPDATE)
    @Override
    public Result changInfo(DishDTO dto) {
        Long dishId = dto.getId();
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            return Result.error(MessageConstant.DISH_NOT_EXIST);
        }
        BeanUtils.copyProperties(dto, dish);
        dishMapper.updateById(dish);
        //获取对应菜品的所有口味
        List<DishFlavor> flavors = dto.getFlavors();
        dishFlavorMapper.updateById(flavors);
        return Result.success();
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @Transactional
    @Override
    public Result deleteByIds(String ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(MessageConstant.DELETE_PARME_NULL);
        }

        List<String> idList = Arrays.stream(ids.split(",")).toList();
        List<Long> dishIdList = idList.stream().map(Long::parseLong).toList();

        for (Long dishId : dishIdList) {
            Dish dish = dishMapper.selectById(dishId);
            if (dish == null) {
                return Result.error(MessageConstant.DISH_NOT_EXIST);
            }
            // 如果被关联，返回错误：MessageConstant.DISH_BE_RELATED_BY_SETMEAL
            SetmealDish setmealDish = setmealDishMapper.selectOne(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getDishId, dishId));
            if (setmealDish != null) {
                return Result.error(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        }
        for (Long dishId : dishIdList) {
            dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dishId));
        }

        dishMapper.deleteBatchIds(dishIdList);

        return Result.success();
    }

    /**
     * 根据分类查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public Result<List<Dish>> queryByCategoryId(String categoryId) {
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, categoryId));
        return Result.success(dishes);
    }

    @Transactional
    @AutoFill(OperationType.UPDATE)
    @Override
    public Result changStatus(String status,String id) {
        //先查询对应id的菜品
        Dish dish = dishMapper.selectById(id);
        if (dish==null){
            return Result.error(MessageConstant.PLEASE_SELECT_CATEGORY);
        }
        //再去套餐中看是否有该菜品
        SetmealDish setmealDish = setmealDishMapper.selectOne(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getDishId, id));
        if(setmealDish!=null){
            return Result.error(MessageConstant.SETMEAL_EXIST_DISH_NOT_STATUS);
        }
        dish.setStatus(Integer.valueOf(status));
        dishMapper.updateById(dish);
        return Result.success();
    }
}
