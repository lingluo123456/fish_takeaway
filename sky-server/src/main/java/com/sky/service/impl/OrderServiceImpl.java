package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired DishMapper dishMapper;



    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //1.先看地址和购物车是否为空。
        AddressBook addressBook=addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //2.向订单表中插入数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//用时间戳生成订单号
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);
        //3.向订单明细表中插入数据
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for(ShoppingCart cart :shoppingCartList){
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);
        //4.清空购物车数据
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
        //5.返回OrderSubmitVO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();
        return orderSubmitVO;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    public OrderVO selectById(Long id) {
        OrderVO orderVO=new OrderVO();
        Orders orders = orderMapper.selectById(id);

        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(id);
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void repetition(Long id) {
        List<ShoppingCart> shoppingCartList= new ArrayList<>();
        List<OrderDetail> orderDetailList =orderDetailMapper.getByOrderId(id);
        for(OrderDetail orderDetail :orderDetailList){
            ShoppingCart shoppingCart=new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public void cancel(Long id) {
        //查看是否有订单
        Orders orders = orderMapper.selectById(id);
        Orders orders1 = Orders.builder()
                .id(id)
                .cancelTime(LocalDateTime.now())
                .status(Orders.CANCELLED)
                .cancelReason("用户取消")
                .build();
        if(orders==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //查看订单状态是否合法
        if (orders.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if(orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            orders1.setPayStatus(Orders.REFUND);
        }
        orderMapper.update(orders1);
    }

    public PageResult page4User(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        Page<Orders> pageInfo = orderMapper.page4User(ordersPageQueryDTO);
        List<OrderVO> orderVOs = new ArrayList<>();
        if (pageInfo != null && !pageInfo.isEmpty()) {
            for (Orders orders : pageInfo) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                //查询订单明细
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
                orderVO.setOrderDetailList(orderDetailList);
                orderVOs.add(orderVO);
            }
        }
        return new PageResult(pageInfo.getTotal(), orderVOs);
    }

    @Override
    public PageResult<OrderVO> page4Admin(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> pageInfo = orderMapper.page4Admin(ordersPageQueryDTO);

        List<OrderVO> orderVOs = new ArrayList<>();
        if (pageInfo != null && !pageInfo.isEmpty()) {
            for (Orders orders : pageInfo) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);

                String orderDishes= DishsToStr(orders);

                orderVO.setOrderDishes(orderDishes);
                orderVOs.add(orderVO);
            }
        }
        return new PageResult<>(pageInfo.getTotal(), orderVOs);


    }

    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.statistics(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.statistics(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.statistics(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }
    private String DishsToStr(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        return orderDetailList.stream()
                .map(a -> a.getName() + "*" + a.getNumber() + ";")
                .collect(Collectors.joining());
    }
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orderDB=orderMapper.selectById(ordersRejectionDTO.getId());
        if (orderDB==null||!orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //如果已经付款，则需要退钱
        if (orderDB.getPayStatus().equals(Orders.PAID)){
            log.info("执行退钱流程");
        }
        //更新状态
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders orders=orderMapper.selectById(ordersCancelDTO.getId());
        if (orders.getPayStatus().equals(Orders.PAID)){
            log.info("执行退款");
        }
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders=orderMapper.selectById(id);
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders=orderMapper.selectById(id);
        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
}
