package com.epam.digital.data.platform.report.config.feign;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

public class FeignErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder defaultErrorDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    var status = HttpStatus.valueOf(response.status());
    var exception = defaultErrorDecoder.decode(methodKey, response);

    if (exception instanceof RetryableException) {
      return exception;
    }

    if (status.is5xxServerError()) {
      return new RetryableException(status.value(), "Redash server error",
          response.request().httpMethod(), null, response.request());
    }

    return exception;
  }
}
