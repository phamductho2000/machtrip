package vn.machtrip.common;

/** Request is well-formed but its parameters contradict each other. Mapped to HTTP 400. */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
