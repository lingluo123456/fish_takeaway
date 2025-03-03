package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    /**
     * 超时未支付自动取消订单
     */
    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder(){
        log.info("开始处理超时订单");
        LocalDateTime time =LocalDateTime.now().plusMinutes(-15);

    }
    /**
     * 定时派送自动完成订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryingOrder(){
        log.info("开始处理派送订单");
        LocalDateTime time =LocalDateTime.now().plusHours(-1);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,time);
        if (ordersList!=null&&!ordersList.isEmpty()){
            for (Orders orders:ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }



}
