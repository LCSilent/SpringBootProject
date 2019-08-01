package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.response.CommomReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception ex) {
        Map<String, Object> responseData = new HashMap<>();
        if (ex instanceof BusinessException) {
            BusinessException businessException = (BusinessException) ex;
            responseData.put("errCode", businessException.getErrCode());
            responseData.put("errMsg", businessException.getErrMsg());
        } else {

            responseData.put("errCode", EmBussinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg", EmBussinessError.UNKNOWN_ERROR.getErrMsg());

        }
        return CommomReturnType.create(responseData, "fail");

//        CommomReturnType commomReturnType = new CommomReturnType();
//        commomReturnType.setStatus("fail");
//
//        commomReturnType.setData(responseData);
//        return commomReturnType;
    }
}
