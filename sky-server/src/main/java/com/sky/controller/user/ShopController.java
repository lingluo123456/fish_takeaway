package com.sky.controller.user;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userShopController")
@RequestMapping("/user/shop")
public class ShopController {

    public static final String KEY="SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;
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
