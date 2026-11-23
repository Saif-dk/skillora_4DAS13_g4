package tn.esprit.skillora.payment.exception;

/** Mapped to HTTP 502 - the external payment provider failed or is unreachable. */
public class PaymentProviderException extends RuntimeException {

    public PaymentProviderException(String message) {
        super(message);
    }

    public PaymentProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
