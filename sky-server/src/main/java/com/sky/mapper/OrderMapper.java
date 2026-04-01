package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    
    /**
     * 根据id查询订单
     * @param id
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 分页条件查询并按下单时间排序
     * @param ordersPageQueryDTO
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单状态和下单时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStautsAndOrderTime(Integer status, LocalDateTime orderTime);


    /**
     * 根据状态统计订单数量
     * @param status
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);




    /**
     * 根据日期查询每天都营业额  和这个代码是一样Double sumByMap(Map map);
     * @return
     */
    @Select("select ifnull(sum(amount),0) from orders where order_time >= #{beginTime} and order_time <#{endTime} and status = 5 ")
    Double selectTurnoverByDate(@Param("beginTime") LocalDate beginTime, @Param("endTime") LocalDate endTime);

    /**
     * 根据日期查询总订单数 Integer countByMap(Map map);
     * @return
     */
    @Select("select count(*) from orders where Date(order_time) >= Date(#{beginTime}) and Date(order_time) <= Date(#{endTime})")
    Integer getTotalOrderCountByDate(@Param("beginTime") LocalDate beginTime);

    /**
     * 根据日期查询有效订单数
     * @return
     */
    @Select("select count(*) from orders where order_time >= #{beginTime} and order_time <= #{endTime} and status = 5")
    Integer getValidOrderCountByDate(@Param("beginTime") LocalDate beginTime,@Param("endTime") LocalDate endTime);



    Integer countByMap(Map map);

    Double sumByMap(Map map);
}
