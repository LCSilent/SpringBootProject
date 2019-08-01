package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDaoMapper;
import com.miaoshaproject.dao.UserPasswordDaoMapper;
import com.miaoshaproject.dataobject.UserDao;
import com.miaoshaproject.dataobject.UserPasswordDao;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import com.sun.tools.internal.ws.wsdl.framework.DuplicateEntityException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDaoMapper userDaoMapper;

    @Autowired
    private UserPasswordDaoMapper userPasswordDaoMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userdaomapper获取对应的用户dataobject
        UserDao userDao = userDaoMapper.selectByPrimaryKey(id);
        System.out.println(userDao.getId());
        if (userDao == null) return null;
        UserPasswordDao userPasswordDao = userPasswordDaoMapper.selectByUserId(userDao.getId());
        System.out.println(userPasswordDao);
        return converFromDataObject(userDao, userPasswordDao);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);

//        if(StringUtils.isEmpty(userModel.getName())||userModel.getGender()==null
//            ||userModel.getAge()==null||StringUtils.isEmpty(userModel.getTelphone())){
//            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
//        }
        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        UserDao userDao = converFromModel(userModel);

        try {
            userDaoMapper.insertSelective(userDao);
        } catch (DuplicateEntityException ex) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "手机号已经重复注册");
        }

        userModel.setId(userDao.getId());

        UserPasswordDao userPasswordDao = converPasswordFromModel(userModel);
        userPasswordDaoMapper.insertSelective(userPasswordDao);

        return;

    }

    @Override
    public UserModel validateLogin(String telphone, String EncrptPassword) throws BusinessException {
        //通过用户的手机信息获取用户登录信息

        UserDao userDao = userDaoMapper.selectByTelphone(telphone);
        System.out.println(userDao.getId());
        if (userDao == null) {
            throw new BusinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDao userPasswordDao = userPasswordDaoMapper.selectByUserId(userDao.getId());

        System.out.println(userPasswordDao);
        UserModel userModel = converFromDataObject(userDao, userPasswordDao);

        System.out.println(userModel.getEncrptPassword());
//        System.out.println(1);
        //比对用户信息加密密码与输入密码是否一致
        if (!StringUtils.equals(EncrptPassword, userModel.getEncrptPassword())) {
            throw new BusinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        return userModel;


    }

    public UserPasswordDao converPasswordFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPasswordDao userPasswordDao = new UserPasswordDao();
        userPasswordDao.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDao.setUserId(userModel.getId());
        return userPasswordDao;
    }

    public UserDao converFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserDao userDao = new UserDao();
        BeanUtils.copyProperties(userModel, userDao);
        return userDao;
    }

    public UserModel converFromDataObject(UserDao userDao, UserPasswordDao userPasswordDao) {
        if (userDao == null) return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDao, userModel);//把对应的userDao copy到userModel中去
        if (userPasswordDao != null) {
            userModel.setEncrptPassword(userPasswordDao.getEncrptPassword());

        }

        return userModel;
    }
}
