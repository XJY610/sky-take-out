package com.sky.service.impl;

import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.mapper.*;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end){
        Map map = new HashMap(); //创建一个HashMap集合对象
        map.put("begin",begin);
        map.put("end",end);
        //查询总订单数
        Integer totalOrderCount = orderMapper.countByMap(map);

        map.put("status", Orders.COMPLETED);
        //营业额
        Double turnover = orderMapper.sumByMap(map);
        if(turnover == null){
            turnover=0.0;
        }
        //有效订单数
        Integer validOrderCount = orderMapper.countByMap(map);
        if(validOrderCount == null){
            validOrderCount=0;
        }
        //订单完成率=有效订单数/总计订单数
        Double orderCompletionRate =0.0;
        if(totalOrderCount != 0 && validOrderCount !=0){
            orderCompletionRate = validOrderCount *1.0 / totalOrderCount;
        }
        //平均客单价=营业额/有效订单数
        Double unitPrice=0.0;
        if(turnover != 0 && validOrderCount != 0.0){
            unitPrice = turnover / validOrderCount;
        }
        //新增用户数
        Integer newUsers = userMapper.countByMap(map);
        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();

    }
    public OrderOverViewVO getOrderOverViewOrders(){
        Map map = new HashMap(); //创建一个HashMap集合对象
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));

        //待接单数量
        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = orderMapper.countByMap(map);
        //待派送数量
        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.countByMap(map);
        //已完成数量
        map.put("status", Orders.COMPLETED);
        Integer completedOrders = orderMapper.countByMap(map);

        //已取消数量
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders= orderMapper.countByMap(map);

        //全部订单
        map.put("status",null);
        Integer allOrders= orderMapper.countByMap(map);
        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }


    public DishOverViewVO getDishOverView() {
        Map map = new HashMap(); //创建一个HashMap集合对象

        // 已启售菜品数量
        map.put("status",1);
        Integer sold = dishMapper.countByMap(map);
        // 已停售菜品数量
        map.put("status",0);
        Integer discontinued = dishMapper.countByMap(map);
        return  DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }


    public SetmealOverViewVO getSetmealOverView() {
        Map map =new HashMap();
        // 已启售套餐数量
        map.put("status",1);
        Integer sold = setmealMapper.countByMap(map);
        // 已停售套餐数量
        map.put("status",0);
        Integer discontinued = setmealMapper.countByMap(map);
        return SetmealOverViewVO
                .builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
