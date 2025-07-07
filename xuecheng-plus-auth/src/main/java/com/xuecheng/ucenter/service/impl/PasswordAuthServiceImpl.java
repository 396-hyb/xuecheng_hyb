package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author hyb
 * @version 1.0
 * @description 账号密码认证
 * @date 2025/1/10
 */
@Slf4j
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {
    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {

        //校验验证码
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if(StringUtils.isBlank(checkcode) || StringUtils.isBlank(checkcodekey)){
            throw new RuntimeException("验证码为空");
        }
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if(!verify){
            throw new RuntimeException("验证码输入错误");
        }

        //账号
        String userName = authParamsDto.getUsername();
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, userName));
        if(user == null){
            throw new RuntimeException("账号不存在");
        }
        //校验密码
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        String userPassword = user.getPassword();
        String authPassword = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(authPassword, userPassword);
        if(!matches){
            throw new RuntimeException("账号或密码错误");
        }
        return xcUserExt;
    }
}
