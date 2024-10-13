package net.ctengine.api.errors;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * A general exception that may be thrown by the occurence of {@link CTError}s.
 */
public class CTException extends RuntimeException {
    private List<CTError> errors;

    /**
     * Creates a new {@link CTError}s exception instance.
     * 
     * @param errors List of {@link CTError}s that occured.
     */
    public CTException(@NotNull List<CTError> errors) {
        this.errors = errors;
    }

    /**
     * Retrieves all {@link CTError}s that occured.
     * 
     * @return List of {@link CTError}s that occured.
     */
    @NotNull
    public List<CTError> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }

    /**
     * Retrieves the first non empty message of an error.
     * 
     * @return Error message.
     */
    @Override
    public String getMessage() {
        for(var error : errors) {
            if(error.message != null && error.message.length() > 0) {
                return error.message;
            }
        }

        return null;
    }

    /**
     * Retrieves the first throwable cause of an error.
     * 
     * @return Throwable cause.
     */
    @Override
    public Throwable getCause() {
        for(var error : errors) {
            if(error.cause != null) {
                return error.cause;
            }
        }

        return null;
    }
}
