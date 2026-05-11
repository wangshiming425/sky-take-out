package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("菜品管理接口")
@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dto
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result insert(@RequestBody DishDTO dto){
        log.info("开始新增菜品,信息为:{}",dto);
        return dishService.insert(dto);
    }

    /**
     * 菜品分页查询
     * @param dto
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> pagequery(DishPageQueryDTO dto){
        log.info("菜品分页查询参数为:{}",dto);
        return dishService.pagequery(dto);
    }

    /**
     * 根据菜品Id查询详细信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据菜品Id查询详细信息")
    public Result<DishVO> getById(@PathVariable("id") String id){
        return dishService.selectById(id);
    }

    /**
     * 修改菜品信息
     * @param dto
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result changIdfo(@RequestBody DishDTO dto){
        return dishService.changInfo(dto);
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteMore(String ids){
        return dishService.deleteByIds(ids);
    }

    /**
     * 根据分类来查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类Id查询菜品")
    public Result<List<Dish>> queryByCategoryId(String categoryId){
        return dishService.queryByCategoryId(categoryId);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品状态")
    public Result changeStatus(@PathVariable("status") String status,String id){
        return dishService.changStatus(status,id);
    }
}
