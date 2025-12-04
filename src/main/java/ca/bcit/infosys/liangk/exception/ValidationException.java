package ca.bcit.infosys.liangk.exception;

public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(400, "VALIDATION_ERROR", message);
    }
}
