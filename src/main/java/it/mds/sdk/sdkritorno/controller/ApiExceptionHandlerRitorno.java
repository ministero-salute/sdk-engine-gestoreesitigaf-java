package it.mds.sdk.sdkritorno.controller;

import it.mds.sdk.sdkritorno.exception.ApiErrorResponse;
import it.mds.sdk.sdkritorno.exception.MonitoraggioFlussiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandlerRitorno {

    @ExceptionHandler(MonitoraggioFlussiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiMonitoraggioFlussiException(
            MonitoraggioFlussiException e) {
        @SuppressWarnings("squid:S2583")
        ApiErrorResponse response = ApiErrorResponse.builder().withError("error")
                .withMessage("errore durante monitoraggio flussi - " + e.getCause() != null ? e.getCause().getMessage() : e.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
