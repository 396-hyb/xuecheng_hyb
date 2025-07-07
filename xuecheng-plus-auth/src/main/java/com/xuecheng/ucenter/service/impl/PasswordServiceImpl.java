package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.PasswordService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hyb
 * @version 1.0
 * @description 密码服务接口实现类
 * @date 2025/1/16
 */
@Service
public class PasswordServiceImpl implements PasswordService {

    @Autowired
    CheckCodeClient checkCodeClient;

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void findpassword(FindPasswordParamsDto findPasswordParamsDto) {
        //校验验证码
        String checkcodekey = findPasswordParamsDto.getCheckcodekey();
        String checkcode = findPasswordParamsDto.getCheckcode();
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if(!verify){
            XueChengPlusException.cast("验证码输入错误");
        }
        //判断两次密码是否一致
        String confirmpwd = findPasswordParamsDto.getConfirmpwd();
        String password = findPasswordParamsDto.getPassword();
        if(StringUtils.isEmpty(confirmpwd) || StringUtils.isEmpty(password)){
            XueChengPlusException.cast("密码和确认密码都要输入");
        }
        if(!confirmpwd.equals(password)){
            XueChengPlusException.cast("确认密码和密码不一致");
        }

        //根据手机号和邮箱查询用户
        String cellphone = findPasswordParamsDto.getCellphone();
        String email = findPasswordParamsDto.getEmail();
        if(StringUtils.isEmpty(cellphone) && StringUtils.isEmpty(email)){
            XueChengPlusException.cast("手机号和电子邮箱至少填写一个");
        }

        XcUser xcUser = null;
        if(!StringUtils.isEmpty(cellphone)){
            LambdaQueryWrapper<XcUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(XcUser::getCellphone, cellphone);
            xcUser = xcUserMapper.selectOne(lambdaQueryWrapper);
        }else{
            LambdaQueryWrapper<XcUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(XcUser::getEmail, email);
            xcUser = xcUserMapper.selectOne(lambdaQueryWrapper);
        }
        if(xcUser == null){
            XueChengPlusException.cast("没有该用户");
        }

        //如果找到,为用户更新为新密码
        xcUser.setPassword(passwordEncoder.encode(password));
        int i = xcUserMapper.updateById(xcUser);
        if(i < 1){
            XueChengPlusException.cast("为用户更新为新密码异常");
        }

    }
}
