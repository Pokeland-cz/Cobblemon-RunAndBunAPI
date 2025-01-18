/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2025, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers API. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctapi.api.errors;

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
