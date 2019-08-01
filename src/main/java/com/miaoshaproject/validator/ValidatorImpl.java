package com.miaoshaproject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Component
public class ValidatorImpl implements InitializingBean {
    private Validator validator;


    public ValidationResult validate(Object bean) {
        final ValidationResult validationResult = new ValidationResult();
        final Set<ConstraintViolation<Object>> constraintValidatorSet = validator.validate(bean);
        if (constraintValidatorSet.size() > 0) {
            validationResult.setHasErrors(true);
            constraintValidatorSet.forEach(constraintViolation -> {
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                validationResult.getErrorMsgMap().put(propertyName, errMsg);
            });
        }
        return validationResult;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate validator通过工厂使其实例化

        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
