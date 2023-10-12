package it.mds.sdk.sdkritorno.exception;

public class MonitoraggioFlussiException extends RuntimeException{
    public MonitoraggioFlussiException() { super(); }

    public MonitoraggioFlussiException(String message) {
        super(message);
    }

    public MonitoraggioFlussiException(String message, Throwable cause) {
        super(message, cause);
    }

    public MonitoraggioFlussiException(Throwable cause) {
        super(cause);
    }

    protected MonitoraggioFlussiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
