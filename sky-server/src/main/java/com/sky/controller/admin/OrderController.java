package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult<OrderVO>> page(OrdersPageQueryDTO ordersPageQueryDTO){
        PageResult<OrderVO> pageResult = orderService.page4Admin(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各状态订单数量
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }
    /**
     * 查看订单详情
     */
    @GetMapping("/details/{id}")
    public Result<OrderVO> details(@PathVariable Long id){
        OrderVO orderVO = orderService.selectById(id);
        return Result.success(orderVO);
    }
    /**
     * 接单
     */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }
    /**
     * 拒单
     */
    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        orderService.cancelByAdmin(ordersCancelDTO);
        return Result.success();
    }
    /**
     * 派送订单
     */
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
    }
    /**
     * 完成订单
     */
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        orderService.complete(id);
        return Result.success();
    }

}
