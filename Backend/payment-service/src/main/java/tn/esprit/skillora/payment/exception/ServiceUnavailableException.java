package tn.esprit.skillora.payment.exception;

/** Mapped to HTTP 503 - a required microservice is unavailable. */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
