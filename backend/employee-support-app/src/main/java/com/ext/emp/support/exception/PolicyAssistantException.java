package com.ext.emp.support.exception;

import com.ext.emp.support.common.exception.ApplicationException;
import com.ext.emp.support.common.exception.ErrorCode;

/** Wraps any failure while invoking the underlying chat model. */
public class PolicyAssistantException extends ApplicationException {

    public PolicyAssistantException(String message, Throwable cause) {
        super(ErrorCode.MODEL_INVOCATION_FAILED, message, cause);
    }
}
