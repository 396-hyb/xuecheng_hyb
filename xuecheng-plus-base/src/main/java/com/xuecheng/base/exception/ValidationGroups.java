package com.xuecheng.base.exception;

import javax.validation.groups.Default;

/**
 * @author hyb
 * @version 1.0
 * @description 校验分组
 * @date 2024/11/13
 */
public class ValidationGroups {
    public interface Insert extends Default {};
    public interface Update extends Default{};
    public interface Delete extends Default{};

}
