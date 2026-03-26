package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressMapper;
import com.sky.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressMapper addressMapper;
    /**
     * 查询登录用户的所有地址
     * @param addressBook
     * @return
     */
    public List<AddressBook> list(AddressBook addressBook){
        log.info("查询登录用户的所有地址：{}",addressBook);
        return addressMapper.list(addressBook);
    }

    /**
     * 新增地址
     * @param addressBook
     */
    public void saveAddress(AddressBook addressBook){
        log.info("新增地址：{}",addressBook);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressMapper.insert(addressBook);

    }
    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    public AddressBook getById(Long id){
        log.info("根据id查询地址：{}",id);
        AddressBook addressBook=addressMapper.getById(id);
        return addressBook;
    }
    /**
     * 根据id修改地址
     *
     * @param addressBook
     */
    public void update(AddressBook addressBook) {
        addressMapper.update(addressBook);
    }
    /**
     * 设置默认地址
     *
     * @param addressBook
     */
    @Transactional
    public void setDefault(AddressBook addressBook) {
        //1、将当前用户的所有地址修改为非默认地址 update address_book set is_default = ? where user_id = ?
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressMapper.updateIsDefaultByUserId(addressBook);

        //2、将当前地址改为默认地址 update address_book set is_default = ? where id = ?
        addressBook.setIsDefault(1);
        addressMapper.update(addressBook);
    }

    /**
     * 根据id删除地址
     *
     * @param id
     */
    public void deleteById(Long id) {
        addressMapper.deleteById(id);
    }

}
