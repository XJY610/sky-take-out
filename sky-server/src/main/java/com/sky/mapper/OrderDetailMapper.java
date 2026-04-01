package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单明细数量
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);
    /**
     * 根据订单id查询订单明细
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    @Select("SELECT " +
            "    d.name, " +
            "    SUM(od.number) AS number " +
            "FROM order_detail od " +
            "JOIN orders o ON od.order_id = o.id " +
            "JOIN dish d ON od.dish_id = d.id " +
            "WHERE o.order_time >= #{beginTime}" +
            "  AND o.order_time <= #{endTime}" +
            "GROUP BY d.name " +
            "ORDER BY number DESC " +
            "LIMIT 10")
    List<Map<String, Object>> getSalesTop10ByDate(@Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);
}
