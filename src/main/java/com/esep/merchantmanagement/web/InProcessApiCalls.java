package com.esep.merchantmanagement.web;

import com.esep.merchantmanagement.api.MerchantApiErrors;
import jakarta.validation.Validator;

import java.util.function.Supplier;

/**
 * Общие правила вызова API продавцов в том же процессе: проверка запроса, как у {@code @Valid},
 * и перевод исключений в те же коды ошибок, что у HTTP API.
 */
class InProcessApiCalls {

    private final Validator validator;

    InProcessApiCalls(Validator validator) {
        this.validator = validator;
    }

    <T> T valid(T request) {
        if (!validator.validate(request).isEmpty()) {
            throw new MerchantApiException(400, "Запрос не прошёл проверку.", null);
        }
        return request;
    }

    <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (MerchantApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new MerchantApiException(
                    MerchantApiErrors.statusOf(exception).value(),
                    MerchantApiErrors.clientMessageOf(exception),
                    exception
            );
        }
    }

    void run(Runnable call) {
        execute(() -> {
            call.run();
            return null;
        });
    }
}
