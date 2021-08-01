package com.epam.digital.data.platform.report.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

public class ResponseHandler {

    private ResponseHandler() {
    }

    public static <T> T handleResponse(ResponseEntity<T> response) {
        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw new ResponseStatusException(response.getStatusCode());
        }
        return response.getBody();
    }
}
