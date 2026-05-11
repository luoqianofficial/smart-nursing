package com.smartnursing.common.exception;

import com.smartnursing.common.result.CommonResult;
import com.smartnursing.common.enums.GlobalErrorCodeConstants;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 出现异常返回错误提示, 并且回滚事务
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonResult exceptionHandler(Exception e){
        System.err.println("捕获到异常");
        e.printStackTrace();
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
    }
}