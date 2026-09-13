package com.ext.emp.support.exception;

import com.ext.emp.support.common.exception.ApplicationException;
import com.ext.emp.support.common.exception.ErrorCode;

public class InvalidCredentialsException extends ApplicationException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
    }
}
