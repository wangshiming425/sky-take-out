package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("分类管理接口")
@Slf4j
@RestController
@RequestMapping("/admin/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 分类分页查询
     *
     * @param dto
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(("分类分页查询"))
    public Result<PageResult> pagequery(CategoryPageQueryDTO dto) {
        return categoryService.pagequery(dto);
    }

    /**
     * 删除分类
     *
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据分类id删除分类")
    public Result deleteById(@RequestParam Integer id) {
        return categoryService.delete(id);
    }

    /**
     * 新增分类
     *
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result insert(@RequestBody CategoryDTO categoryDTO) {
        return categoryService.insert(categoryDTO);
    }

    /**
     * 修改分类信息
     *
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result changeInFo(@RequestBody CategoryDTO categoryDTO) {
        return categoryService.changInFo(categoryDTO);
    }

    /**
     * 修改分类状态
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改分类状态")
    public Result changStatus(@PathVariable("status") Integer status, String id) {
        return categoryService.changStatus(status, id);
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> selectByType(String type){
        return categoryService.selectByType(type);
    }
}
