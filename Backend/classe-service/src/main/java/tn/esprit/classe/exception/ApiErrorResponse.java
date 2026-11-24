package tn.esprit.classe.exception;

public record ApiErrorResponse(String timestamp, int status, String message) {
}
