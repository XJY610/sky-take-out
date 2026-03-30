package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    @Scheduled(cron = "0 * * * * ?")
    //@Scheduled(cron = " * * * * * ? ") // 每一秒都触发一次
    //每分钟触发一次
    public void  processTimeoutOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //查询订单状态为“待付款” 且超过15分钟
        //select * from orders where status = ? and order_time<(nowtime-15min)
        List<Orders> ordersList = orderMapper.getByStautsAndOrderTime(Orders.PENDING_PAYMENT,time);
        if (ordersList != null && ordersList.size()>0){
            for(Orders orders:ordersList){
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("支付超时，订单自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }

    }
    @Scheduled(cron = "0 0 1 * * ?")
    //@Scheduled(cron = "*/5 * * * * ?")
// 修正：使用 */5 代表每5秒，而不是 1/5
    public void  processDeliveryOrder(){
        log.info("定时处理处于待派送状态的订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStautsAndOrderTime(Orders.DELIVERY_IN_PROGRESS,time);
        if (ordersList != null && ordersList.size()>0){
            for(Orders orders:ordersList){
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);

            }
        }

    }
}
