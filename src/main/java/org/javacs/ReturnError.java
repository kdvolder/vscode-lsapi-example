package org.javacs;

public class ReturnError extends RuntimeException {
    public final String message;

    ReturnError(String message, Exception cause) {
        super(cause);

        this.message = message;
    }
}
