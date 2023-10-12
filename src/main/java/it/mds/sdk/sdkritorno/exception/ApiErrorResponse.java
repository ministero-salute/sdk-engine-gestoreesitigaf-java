package it.mds.sdk.sdkritorno.exception;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(setterPrefix = "with")
public class ApiErrorResponse {
    String error;
    String message;

    public ApiErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }
}
