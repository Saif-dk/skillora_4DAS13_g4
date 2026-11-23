package tn.esprit.skillora.payment.exception;

/** Mapped to HTTP 409 - the request conflicts with the current state of the resource. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
