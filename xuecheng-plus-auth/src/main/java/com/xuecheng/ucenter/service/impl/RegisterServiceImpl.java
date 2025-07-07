package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcRoleMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.RegisterParamsDto;
import com.xuecheng.ucenter.model.po.XcRole;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author hyb
 * @version 1.0
 * @description 注册用户接口实现类
 * @date 2025/1/17
 */
@Slf4j
@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    CheckCodeClient checkCodeClient;

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    XcUserRoleMapper xcUserRoleMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    XcRoleMapper xcRoleMapper;

    @Transactional
    @Override
    public void register(RegisterParamsDto registerParamsDto) {

        //校验验证码
        String checkcodekey = registerParamsDto.getCheckcodekey();
        String checkcode = registerParamsDto.getCheckcode();
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if(!verify){
            XueChengPlusException.cast("验证码输入错误");
        }
        //判断两次密码是否一致
        String confirmpwd = registerParamsDto.getConfirmpwd();
        String password = registerParamsDto.getPassword();
        if(StringUtils.isEmpty(confirmpwd) || StringUtils.isEmpty(password)){
            XueChengPlusException.cast("密码和确认密码都要输入");
        }
        if(!confirmpwd.equals(password)){
            XueChengPlusException.cast("确认密码和密码不一致");
        }

        //根据手机号和邮箱查询用户
        String cellphone = registerParamsDto.getCellphone();
        String email = registerParamsDto.getEmail();
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
        if(xcUser != null){
            log.debug("已存在该用户，直接返回");
            XueChengPlusException.cast("已存在该用户");
            return;
        }

        //查询账号是否唯一
        String username = registerParamsDto.getUsername();
        if(StringUtils.isEmpty(username)){
            XueChengPlusException.cast("账号不能为空");
        }
        XcUser xcUser1 = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if(xcUser1 != null){
            log.debug("已存在该账号名");
            XueChengPlusException.cast("已存在该账号名");
        }

        //向用户表插入
        xcUser = new XcUser();
        xcUser.setUsername(username);
        xcUser.setPassword(passwordEncoder.encode(password));
        xcUser.setName(username);
        xcUser.setNickname(registerParamsDto.getNickname());
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCellphone(cellphone);
        xcUser.setEmail(email);
        xcUser.setCreateTime(LocalDateTime.now());
        int insertXcUser = xcUserMapper.insert(xcUser);
        if(insertXcUser < 1){
            log.debug("向用户表插入失败");
            XueChengPlusException.cast("保存用户失败");
        }

        //向用户角色关系表插入
        XcUserRole xcUserRole = new XcUserRole();
        XcRole student = xcRoleMapper.selectOne(new LambdaQueryWrapper<XcRole>().eq(XcRole::getRoleCode, "student"));
        xcUserRole.setUserId(xcUser.getId());
        xcUserRole.setRoleId(student.getId());
        xcUserRole.setCreateTime(LocalDateTime.now());
        int insertXcUserRole = xcUserRoleMapper.insert(xcUserRole);
        if(insertXcUserRole < 1){
            log.debug("向用户角色关系表插入失败");
            XueChengPlusException.cast("保存用户角色失败");
        }
    }
}
