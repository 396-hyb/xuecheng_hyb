package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.RegisterParamsDto;

/**
 * @author hyb
 * @version 1.0
 * @description 注册用户接口
 * @date 2025/1/17
 */
public interface RegisterService {

    void register(RegisterParamsDto registerParamsDto);

}
