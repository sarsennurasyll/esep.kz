package com.esep.merchantmanagement.api;

import com.esep.merchantmanagement.exception.MerchantAliasAlreadyExistsException;
import com.esep.merchantmanagement.exception.MerchantDeletionNotAllowedException;
import com.esep.merchantmanagement.exception.MerchantNotFoundException;
import com.esep.merchantmanagement.exception.MerchantVersionConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Единое соответствие ошибок управления продавцами HTTP-статусам.
 * Используется REST-обработчиком и web-клиентами, вызывающими API в том же процессе.
 */
public final class MerchantApiErrors {

    public static final String INTERNAL_ERROR_MESSAGE = "Внутренняя ошибка сервера";

    private MerchantApiErrors() {
    }

    public static HttpStatus statusOf(Throwable exception) {
        if (exception instanceof MerchantNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception instanceof MerchantAliasAlreadyExistsException || exception instanceof MerchantDeletionNotAllowedException
                || exception instanceof MerchantVersionConflictException
                || exception instanceof ObjectOptimisticLockingFailureException) {
            return HttpStatus.CONFLICT;
        }
        if (exception instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public static String clientMessageOf(Throwable exception) {
        return statusOf(exception).is5xxServerError() ? INTERNAL_ERROR_MESSAGE : exception.getMessage();
    }
}
