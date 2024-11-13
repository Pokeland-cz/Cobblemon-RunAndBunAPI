package net.ctengine.api.ai.learning;

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