package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {
    /**
     * 获取营业数据
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 获取订单统计数据
     * @return
     */
    OrderOverViewVO getOrderOverViewOrders();

    /**
     * 获取菜品总览
     * @return
     */

    DishOverViewVO getDishOverView();

    /**
     * 获取套餐总览
     * @return
     */
    SetmealOverViewVO getSetmealOverView();
}
