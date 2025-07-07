package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;

/**
 * @author hyb
 * @version 1.0
 * @description 密码服务接口
 * @date 2025/1/16
 */

public interface PasswordService {
    void findpassword(FindPasswordParamsDto findPasswordParamsDto);

}
