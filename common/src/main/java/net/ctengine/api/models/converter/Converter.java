package net.ctengine.api.models.converter;

import net.ctengine.api.errors.RCTErrors;
import net.ctengine.api.errors.RCTException;

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
    TTarget toTarget(TSource source, RCTErrors<RCTException> errors);
}
