package net.ctengine.api.models.converter;

import net.ctengine.api.errors.CTErrors;
import net.ctengine.api.errors.CTException;

/**
 * Functional interface that declares the conversion from a source to a
 * target type.
 */
public interface Converter<TSource, TTarget> {
    /**
     * Converts the given source to a target instance.
     * 
     * @param source Source object to convert.
     * @param errors Error collector.
     * @return New target instance.
     */
    TTarget toTarget(TSource source, CTErrors<CTException> errors);
}
