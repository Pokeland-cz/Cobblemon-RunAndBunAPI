package net.ctengine.api.errors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * Service to collect and react to multiple {@link CTError}s.
 */
public class CTErrors<T extends CTException> {
    private final Function<List<CTError>, T> exceptionInitializer;
    private List<CTError> errors = new ArrayList<>();

    private CTErrors(@NotNull Function<List<CTError>, T> exceptionInitializer) {
        this.exceptionInitializer = exceptionInitializer;
    }

    /**
     * Creates a new error collection instance.
     * 
     * @return New error collection instance.
     */
    public static CTErrors<CTException> create() {
        return create(CTException::new);
    }

    /**
     * Creates a new error collection instance.
     * 
     * @param <V> {@link CTException} type parameter.
     * @param exceptionInitializer Initializer function to instantiate a new exception of type V.
     * @return New error collection instance.
     */
    public static <V extends CTException> CTErrors<V> create(@NotNull Function<List<CTError>, V> exceptionInitializer) {
        return new CTErrors<V>(exceptionInitializer);
    }

    /**
     * Adds a new {@link CTError} to this error collection.
     * 
     * @param error {@link CTError} to add.
     * @return This error collection.
     */
    public CTErrors<T> add(@NotNull CTError error) {
        this.errors.add(error);
        return this;
    }

    /**
     * Tests the provided value with the given predicate and adds an {@link CTError}
     * to this error collection if the test fails.
     * 
     * @param <V> Type of the value to test.
     * @param got Value to test.
     * @param predicate Test function.
     * @return The provided value.
     */
    public <V> V expect(V got, Function<V, Boolean> predicate) {
        return expect(got, predicate, () -> got, "invalid value '%s'");
    }

    /**
     * Tests the provided value with the given predicate and adds an {@link CTError}
     * to this error collection if the test fails.
     * 
     * @param <V> Type of the value to test.
     * @param got Value to test.
     * @param predicate Test function.
     * @param format Format string used to create the error message (may contain exactly one '%s' placeholder).
     * @return The provided value.
     */
    public <V> V expect(V got, Function<V, Boolean> predicate, String format) {
        return expect(got, predicate, () -> got, format);
    }

    /**
     * Tests the provided value with the given predicate and adds an {@link CTError}
     * to this error collection if the test fails.
     * 
     * @param <V> Type of the value to test.
     * @param got Value to test.
     * @param predicate Test function.
     * @param otherwise Alternative value supplier.
     * @return The provided value if the test succeeded or the result of otherwise if it fails.
     */
    public <V> V expect(V got, Function<V, Boolean> predicate, Supplier<V> otherwise) {
        return expect(got, predicate, otherwise, "invalid value '%s'");
    }

    /**
     * Tests the provided value with the given predicate and adds an {@link CTError}
     * to this error collection if the test fails.
     * 
     * @param <V> Type of the value to test.
     * @param got Value to test.
     * @param predicate Test function.
     * @param otherwise Alternative value supplier.
     * @param format Format string used to create the error message (may contain exactly one '%s' placeholder).
     * @return The provided value if the test succeeded or the result of otherwise if it fails.
     */
    public <V> V expect(V got, Function<V, Boolean> predicate, Supplier<V> otherwise, String format) {        
        if(!predicate.apply(got)) {
            if(format.contains("%s")) {
                format = String.format(format, got != null ? got.toString() : got);
            }

            this.add(CTError.of(format));
            return otherwise.get();
        }

        return got;
    }

    /**
     * Checks if at least one {@link CTError} occured and throws a new {@link CTException}
     * if this is the case.
     * 
     * @throws T If atleast one {@link CTError} occured.
     */
    public void check() {
        if(this.errors.size() > 0) {
            throw this.exceptionInitializer.apply(this.errors);
        }
    }
}
