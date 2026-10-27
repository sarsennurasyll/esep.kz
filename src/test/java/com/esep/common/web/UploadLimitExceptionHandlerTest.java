package com.esep.common.web;

import com.esep.common.api.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;

class UploadLimitExceptionHandlerTest {

    private final UploadLimitExceptionHandler handler = new UploadLimitExceptionHandler();

    @Test
    void shouldReturnJsonPayloadTooLargeForApi() {
        Object result = handler.handle(new MockHttpServletRequest("POST", "/api/statements/import"), new MockHttpServletResponse());

        assertThat(result).isInstanceOfSatisfying(ResponseEntity.class, response -> {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
            assertThat(response.getBody()).isEqualTo(new ApiErrorResponse(UploadLimitExceptionHandler.MESSAGE, "UPLOAD_TOO_LARGE"));
        });
    }

    @Test
    void shouldRenderPayloadTooLargePageForWebForm() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        Object result = handler.handle(new MockHttpServletRequest("POST", "/import"), response);

        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(result).isInstanceOfSatisfying(ModelAndView.class, view -> assertThat(view.getViewName()).isEqualTo("error/413"));
    }
}
