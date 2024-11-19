package net.ctengine.api.errors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * Service to collect and react to multiple {@link RCTError}s.
 */
public class RCTErrors<T extends RCTException> {
    private final Function<List<RCTError>, T> exceptionInitializer;
    private List<RCTError> errors = new ArrayList<>();

    private RCTErrors(@NotNull Function<List<RCTError>, T> exceptionInitializer) {
        this.exceptionInitializer = exceptionInitializer;
    }

    /**
     * Creates a new error collection instance.
     * 
     * @return New error collection instance.
     */
    public static RCTErrors<RCTException> create() {
        return create(RCTException::new);
    }

    /**
     * Creates a new error collection instance.
     * 
     * @param <V> {@link RCTException} type parameter.
     * @param exceptionInitializer Initializer function to instantiate a new exception of type V.
     * @return New error collection instance.
     */
    public static <V extends RCTException> RCTErrors<V> create(@NotNull Function<List<RCTError>, V> exceptionInitializer) {
        return new RCTErrors<V>(exceptionInitializer);
    }

    /**
     * Adds a new {@link RCTError} to this error collection.
     * 
     * @param error {@link RCTError} to add.
     * @return This error collection.
     */
    public RCTErrors<T> add(@NotNull RCTError error) {
        this.errors.add(error);
        return this;
    }

    /**
     * Tests the provided value with the given predicate and adds an {@link RCTError}
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
     * Tests the provided value with the given predicate and adds an {@link RCTError}
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
     * Tests the provided value with the given predicate and adds an {@link RCTError}
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
     * Tests the provided value with the given predicate and adds an {@link RCTError}
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

            this.add(RCTError.of(format));
            return otherwise.get();
        }

        return got;
    }

    public <V> boolean doif(V got, Function<V, Boolean> predicate, Consumer<V> then) {
        return doif(got, predicate, then, "invalid value '%s'");
    }

    public <V> boolean doif(V got, Function<V, Boolean> predicate, Consumer<V> then, String format) {
        if(!predicate.apply(got)) {
            if(format.contains("%s")) {
                format = String.format(format, got != null ? got.toString() : got);
            }

            this.add(RCTError.of(format));
            return false;
        }
        
        then.accept(got);
        return true;
    }

    /**
     * Checks if at least one {@link RCTError} occured and throws a new {@link RCTException}
     * if this is the case.
     * 
     * @throws T If atleast one {@link RCTError} occured.
     */
    public void check() {
        if(this.errors.size() > 0) {
            throw this.exceptionInitializer.apply(this.errors);
        }
    }
}
