package com.smartnursing.common.exception;

import com.smartnursing.common.result.CommonResult;
import com.smartnursing.common.enums.GlobalErrorCodeConstants;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理业务逻辑异常
     * @param e 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public CommonResult<?> handleServiceException(ServiceException e){
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理系统异常
     * @param e 系统异常
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonResult<?> handleException(Exception e){
        e.printStackTrace();
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
    }


}