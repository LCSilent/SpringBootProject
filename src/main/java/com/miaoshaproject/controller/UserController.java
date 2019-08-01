package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewObject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.response.CommomReturnType;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*") //处理ajax跨域请求
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping(value = "/login", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommomReturnType login(@RequestParam(name = "telphone") String telphone,
                                  @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if (org.apache.commons.lang3.StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        //用户登录服务，用来校验用户登录是否合法
        UserModel userModel = userService.validateLogin(telphone, this.EncoderByMD5(password));

        //加入到登录成功的session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);
        return CommomReturnType.create(null);

    }


    //用户注册接口
    @RequestMapping(value = "/register", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody

    public CommomReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "password") String password) throws BusinessException, NoSuchAlgorithmException, UnsupportedEncodingException {
        //验证手机号与对应的Otpcode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if (!com.alibaba.druid.util.StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
        }
//        System.out.println(1);
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncoderByMD5(password));
//        System.out.println(1);
        userService.register(userModel);
//        System.out.println(1);
        return CommomReturnType.create(null);


        //用户注册流程
    }


    public String EncoderByMD5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();

        String newStr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }

    //用户获取otp短信借口
    @RequestMapping(value = "/getotp", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody

    public CommomReturnType getOtp(@RequestParam(name = "telphone") String telphone) {
        //需要按照一定的规则生成otp验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);


        //将otp验证码同对应的用户手机号关联
        httpServletRequest.getSession().setAttribute(telphone, otpCode);


        //将otp验证码通过短信通道发送给用户，省略

        System.out.println("telphone = " + telphone + " & Otp = " + otpCode);


        return CommomReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommomReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        //调用Service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);
        if (userModel == null) {
//            userModel.setEncrptPassword("123");
            throw new BusinessException(EmBussinessError.USER_NOT_EXIST);
        }

        UserVO userVO = convertFromModel(userModel);

        return CommomReturnType.create(userVO);

    }

    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }

    //定义Exceptionhandler解决未被controller层吸收的exception


}
