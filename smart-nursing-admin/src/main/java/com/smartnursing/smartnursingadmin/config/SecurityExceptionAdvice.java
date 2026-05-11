package com.smartnursing.smartnursingadmin.config;

import com.smartnursing.common.enums.GlobalErrorCodeConstants;
import com.smartnursing.common.result.CommonResult;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 方法级鉴权等场景下的 Spring Security 异常，转为统一业务错误码（避免落入 common 的全局 Exception 变成 500）。
 */
@Order(-1)
@RestControllerAdvice
public class SecurityExceptionAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    public CommonResult<Void> handleAccessDenied(AccessDeniedException ignored) {
        return CommonResult.error(GlobalErrorCodeConstants.FORBIDDEN);
    }
}
