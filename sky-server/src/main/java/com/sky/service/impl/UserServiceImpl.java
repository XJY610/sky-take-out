package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    //定义微信接口地址常量
    public static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties wechatProperties;
    @Autowired
    private UserMapper userMapper;
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    public User wxlogin( UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());

        //判断openid是否为空，如果为空，说明登录失败，抛出异常，
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //如果不为空，判断当前用户是否为新用户，
        User user =userMapper.getByOpenid(openid);
        // 如果是新用户，自动完成注册 自己封装user对象保存到用户表中  x.builder()是构建器
        if(user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户对象
        return user;
    }

    private String getOpenid(String code) {
        // 创建一个 map 对象，封装参数
        Map<String, String> map = new HashMap<>();
        map.put("appid", wechatProperties.getAppId());
        map.put("secret", wechatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");

        try {
            // 调用微信接口服务
            String json = HttpClientUtil.doGet(WECHAT_LOGIN_URL, map);

            // 【关键调试】打印微信接口返回的原始 JSON
            log.info("微信接口返回原始数据: {}", json);

            // 判空处理
            if (json == null || json.trim().isEmpty()) {
                log.error("调用微信接口返回内容为空");
                return null;
            }

            // 创建 JSON 对象
            JSONObject jsonObject = JSON.parseObject(json);

            // 【关键逻辑】检查微信是否返回了错误码 (errcode)
            // 正常成功是没有 errcode 字段的，只有 openid 和 session_key
            if (jsonObject.containsKey("errcode")) {
                int errCode = jsonObject.getIntValue("errcode");
                String errMsg = jsonObject.getString("errmsg");
                log.error("微信接口调用失败 -> errcode: {}, errmsg: {}", errCode, errMsg);
                return null;
            }

            // 获取 openid
            String openid = jsonObject.getString("openid");

            if (openid == null || openid.trim().isEmpty()) {
                log.error("微信接口返回数据中未找到 openid 字段，原始数据: {}", json);
                return null;
            }

            return openid;

        } catch (Exception e) {
            log.error("获取 openid 过程中发生异常", e);
            return null;
        }
    }
}


