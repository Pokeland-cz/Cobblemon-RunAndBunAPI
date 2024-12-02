/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2024, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctapi.api.ai.experimental.lai;

import java.util.function.Supplier;

class RatedAction<T> {
    public static final double MIN_RATING = 0;
    public static final double MAX_RATING = 1;

    private Supplier<T> supplier;
    private double rating;

    public RatedAction() {
        this(null, MIN_RATING + (MAX_RATING - MIN_RATING)/2);
    }

    public RatedAction(Supplier<T> supplier) {
        this(supplier, MIN_RATING + (MAX_RATING - MIN_RATING)/2);
    }

    public RatedAction(double rating) {
        this(null, rating);
    }
    
    public RatedAction(Supplier<T> supplier, double rating) {
        this.supplier = supplier;
        this.rating = Math.min(MAX_RATING, Math.max(MIN_RATING, rating));
    }
    
    public RatedAction<T> withSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
        return this;
    }

    public double getRating() {
        return this.rating;
    }

    public void addRating(double value) {
        this.rating = Math.max(MIN_RATING, Math.min(MAX_RATING, this.rating + value));
    }

    public T get() {
        return this.supplier.get();
    }
}
