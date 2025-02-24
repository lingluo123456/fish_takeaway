package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 条件分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询");
        PageResult pageResult =dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 新增菜品
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品");
        dishService.saveWithFlavor(dishDTO);

        String key ="dish_"+dishDTO.getCategoryId();
        redisDelete(key);
        return Result.success();
    }


    /**
     * 根据id查询菜品
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品");
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }
    /**
     * 修改菜品
     */
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品");
        dishService.updateWithFlavor(dishDTO);

        redisDelete("dish_*");
        return Result.success();
    }
    /**
     * 批量删除菜品
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除菜品");
        dishService.deleteByIds(ids);

        redisDelete("dish_*");
        return Result.success();
    }
    /**
     * 菜品起售停售
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,@RequestParam Long id){
        log.info("菜品起售停售");
        dishService.startOrStop(status,id);

        redisDelete("dish_*");
        return Result.success();
    }
    @GetMapping("/list")
    public Result list(@RequestParam Long categoryId){
        List<DishVO> list = dishService.list(categoryId);
        return Result.success(list);
    }

    private void redisDelete(String patten) {
        Set keys = redisTemplate.keys(patten);
        redisTemplate.delete(keys);
    }
}
