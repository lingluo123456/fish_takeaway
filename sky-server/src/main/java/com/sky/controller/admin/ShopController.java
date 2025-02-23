package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@RequestMapping("/admin/shop")
public class ShopController {

    public static final String KEY="SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置营业状态
     */
    @PutMapping("/{status}")
    public Result setShopStatus(@PathVariable Integer status){
        log.info("设置营业状态为：{}",status==1 ?"营业中":"未营业");
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }
    /**
     * 查询营业状态
     */
    @GetMapping("/status")
    public Result getShopStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("设置营业状态为：{}",status==1 ?"营业中":"未营业");
        return Result.success(status);
    }
}
