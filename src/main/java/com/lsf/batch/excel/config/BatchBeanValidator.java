package com.lsf.batch.excel.config;

import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;
import org.springframework.beans.factory.InitializingBean;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * Bean Validator
 * Created by lishenfei on 2016-12-15.
 */
public class BatchBeanValidator<T> implements Validator<T>, InitializingBean {

    private javax.validation.Validator validator;

    @Override
    public void validate(T value) throws ValidationException {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(value); // Validator

        if (constraintViolations.size() > 0) {
            StringBuilder message = new StringBuilder();
            for (ConstraintViolation<T> violation : constraintViolations) {
                message.append(violation.getPropertyPath() + " " + violation.getMessage() + "\n");
            }
            throw new ValidationException(message.toString());
        }
    }

    /**
     * Init Validator, Use JSR-303 Validator
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }
}
