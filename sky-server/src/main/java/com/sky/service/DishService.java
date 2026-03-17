package com.sky.service;

import com.sky.dto.DishDTO;
import org.springframework.beans.factory.annotation.Autowired;

public interface DishService {

    /**
     * 新增菜品和对应口味
     */
    void saveWithFlavor(DishDTO dishDTO);
}
