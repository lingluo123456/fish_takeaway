package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public Result submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单");
        OrderSubmitVO orderSubmitVO =  orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        OrderPaymentVO orderPaymentVO = new OrderPaymentVO();
        return Result.success(orderPaymentVO);
    }
    /**
     * 历史订单查询
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> page(int page, int pageSize, Integer status) {
        PageResult pageResult = orderService.page4User(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 订单详情查询
     */

    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> selectById(@PathVariable Long id) {
        OrderVO orderVO= orderService.selectById(id);
        return Result.success(orderVO);
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @PostMapping("repetition/{id}")
    public Result repetition(@PathVariable Long id){
        orderService.repetition(id);
        return Result.success();
    }
    /**
     * 取消订单
     */
    @PutMapping("cancel/{id}")
    public Result cancel(@PathVariable Long id){
        orderService.cancel(id);
        return Result.success();
    }
}
