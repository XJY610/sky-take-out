package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);
    /**
     * 根据id查询用户
     * @param userId
     * @return
     */
    @Select("SELECT * FROM user WHERE id = #{userId}")
    User getById(Long userId);

    /**
     * 统计每天的新增用户 Integer countByMap(Map map);
     * @param date
     * @return
     */
    @Select("select count(*) from user where date(create_time) =#{date}")
    Integer getNewUsersByDate(LocalDate date);
    @Select("select count(*) from user where create_time >= #{beginTime} and create_time < #{endTime}")
    Integer getUserByDate(@Param("beginTime") LocalDate beginTime, @Param("endTime") LocalDate endTime);

    Integer countByMap(Map map);
}
