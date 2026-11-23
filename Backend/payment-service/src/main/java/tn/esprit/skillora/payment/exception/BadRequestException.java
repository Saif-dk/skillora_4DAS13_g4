package tn.esprit.skillora.payment.exception;

/** Mapped to HTTP 400 - the request is invalid. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
