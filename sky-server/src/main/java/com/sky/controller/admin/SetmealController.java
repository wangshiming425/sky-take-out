package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api("套餐管理接口")
@Slf4j
@RestController
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     *
     * @param dto
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result insert(@RequestBody SetmealDTO dto) {
        return setmealService.insert(dto);
    }

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO dto) {
        return setmealService.pageQuery(dto);
    }

    /**
     * 查询单个套餐具体信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable("id") String id) {
        return setmealService.selectById(id);
    }

    /**
     * 修改套餐
     *
     * @param dto
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    public Result changInfo(@RequestBody SetmealDTO dto) {
        return setmealService.changIndo(dto);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除")
    public Result deleteMore(String ids) {
        return setmealService.delete(ids);
    }

    /**
     * 修改套餐状态
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改售卖状态")
    public Result changeStatus(@PathVariable("status") String status, String id) {
        return setmealService.changstatus(status, id);
    }
}
