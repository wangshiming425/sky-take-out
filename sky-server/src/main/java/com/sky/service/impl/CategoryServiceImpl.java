package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 分类分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public Result<PageResult> pagequery(CategoryPageQueryDTO dto) {
        int page = dto.getPage();
        int pageSize = dto.getPageSize();
        Page<Category> categoryPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        String name = dto.getName();
        Integer type = dto.getType();
        queryWrapper.like(Strings.isNotBlank(name), Category::getName, name).eq(type != null, Category::getType, type).orderByAsc(Category::getSort);
        Page resultPage = categoryMapper.selectPage(categoryPage, queryWrapper);
        PageResult result = PageResult.builder().total(resultPage.getTotal()).records(resultPage.getRecords()).build();
        return Result.success(result);
    }

    /**
     * 删除分类
     *
     * @param id
     * @return
     */
    @Override
    public Result delete(Integer id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.error(MessageConstant.CATEGORY_NOT_EXIST);
        }
        categoryMapper.deleteById(category);
        return Result.success();
    }

    /**
     * 新增分类
     *
     * @param categoryDTO
     * @return
     */
    @AutoFill(OperationType.INSERT)
    @Override
    public Result insert(CategoryDTO categoryDTO) {
        //先查看分类名称是否存在,应该要保证唯一
        String name = categoryDTO.getName();
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>().eq(Category::getName, name));
        if (category != null) {
            return Result.error(MessageConstant.CATEGORY_HAVIED_EXISTS);
        }
        Category newCategory = new Category();
        BeanUtils.copyProperties(categoryDTO, newCategory);
        newCategory.setStatus(StatusConstant.ENABLE);
        categoryMapper.insert(newCategory);
        return Result.success();
    }

    /**
     * 修改分类信息
     *
     * @param categoryDTO
     * @return
     */
    @AutoFill(OperationType.UPDATE)
    @Override
    public Result changInFo(CategoryDTO categoryDTO) {
        Long id = categoryDTO.getId();
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.error(MessageConstant.CATEGORY_NOT_EXIST);
        }
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.updateById(category);
        return Result.success();
    }

    /**
     * 修改分类状态
     *
     * @param status
     * @param id
     * @return
     */
    @AutoFill(OperationType.UPDATE)
    @Override
    public Result changStatus(Integer status, String id) {
        //先判断对应分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.error(MessageConstant.CATEGORY_NOT_EXIST);
        }
        category.setStatus(status);
        categoryMapper.updateById(category);
        return Result.success();
    }

    @Override
    public Result<List<Category>> selectByType(String type) {
        List<Category> categoryList = categoryMapper.selectList(new LambdaQueryWrapper<Category>().
                eq(Strings.isNotBlank(type), Category::getType, type));
        return Result.success(categoryList);
    }
}
