package com.ext.emp.support.exception;

import com.ext.emp.support.common.exception.ApplicationException;
import com.ext.emp.support.common.exception.ErrorCode;

public class UnknownEmployeeException extends ApplicationException {

    public UnknownEmployeeException(String employeeId) {
        super(ErrorCode.NOT_FOUND, "No employee found with id '%s'".formatted(employeeId));
    }
}
