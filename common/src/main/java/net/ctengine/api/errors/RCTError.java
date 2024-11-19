package net.ctengine.api.errors;

/**
 * General error representation.
 */
public class RCTError {
    /**
     * Error message.
     */
    public final String message;

    /**
     * Throwable that caused the error.
     */
    public final Throwable cause;

    /**
     * Creates a new error instance with the given message and no cause.
     * 
     * @param message Error message string.
     * @return New error instance.
     */
    public static RCTError of(String message) {
        return new RCTError(message, null);
    }
    
    /**
     * Creates a new error instance for given cause. The error message is set to the
     * message of the throwable if not null and to null otherwise.
     * 
     * @param cause Throwable that caused this error.
     * @return New error instance.
     */
    public static RCTError of(Throwable cause) {
        return new RCTError(cause != null && cause.getMessage() != null ? cause.getMessage() : null, cause);
    }

    /**
     * Creates a new error instance for the given cause and with the provided message.
     * 
     * @param message Error message string.
     * @param cause Throwable that caused this error.
     * @return New error instance.
     */
    public static RCTError of(String message, Throwable cause) {
        return new RCTError(message, cause);
    }

    private RCTError(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    @Override
    public String toString() {
        return this.message != null ? this.message : super.toString();
    }
}
