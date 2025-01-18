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

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * A general exception that may be thrown by the occurence of {@link RCTError}s.
 */
public class RCTException extends RuntimeException {
    private List<RCTError> errors;

    /**
     * Creates a new {@link RCTError}s exception instance.
     * 
     * @param errors List of {@link RCTError}s that occured.
     */
    public RCTException(@NotNull List<RCTError> errors) {
        this.errors = errors;
    }

    /**
     * Retrieves all {@link RCTError}s that occured.
     * 
     * @return List of {@link RCTError}s that occured.
     */
    @NotNull
    public List<RCTError> getErrors() {
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
