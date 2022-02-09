package pro.gravit.simplecabinet.web.exception;

public class RegisterException extends AbstractCabinetException {

    public RegisterException(String message) {
        super(message, 1);
    }

    public RegisterException(String message, int code) {
        super(message, 1 + code);
    }
}
