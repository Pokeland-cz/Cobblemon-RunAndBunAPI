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
package com.gitlab.srcmc.rctapi.api.util;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Arbitrary text that may be defined as literal or by providing a language key
 * (translatable). If both, a literal and translatable are provided the
 * translatable takes precedence over the literal. The literal may serve as
 * fallback in case no translation was found.
 * 
 * A Text object may be parsed directly from a string to construct it with a
 * literal.
 */
public class Text implements Serializable, Comparable<Text> {
    private static final long serialVersionUID = 0L;
    private static Gson GSON = new Gson();
    
    private String literal, translatable;
    private transient Cache cache;

    protected Text() {
    }

    /**
     * Constructs a new empty Text.
     * 
     * @return Empty Text.
     */
    public static Text empty() {
        return new Text();
    }

    /**
     * Constructs a new Text with the given literal.
     * 
     * @param literal Literal of the Text.
     * @return Literal Text.
     */
    public static Text literal(String literal) {
        return new Text().setLiteral(literal);
    }

    /**
     * Constructs a new Text with the given translatable (language key).
     * 
     * @param tranlatable Language key.
     * @return Translatable Text.
     */
    public static Text translatable(String tranlatable) {
        return new Text().setTranslatable(tranlatable);
    }
    
    /**
     * Retrieves the configured literal of this Text.
     * 
     * @return Literal of this text or null.
     */
    public String getLiteral() {
        return this.literal;
    }

    /**
     * Retrieves the configured language key of this Text.
     * 
     * @return Language key of this text or null.
     */
    public String getTranslatable() {
        return this.translatable;
    }

    /**
     * Reports if this Text is empty, which is the case if both, the literal and the
     * translatable, are either null or empty.
     * 
     * @return True if the Text is empty.
     */
    public boolean isEmpty() {
        return (this.literal == null || this.literal.isEmpty())
            && (this.translatable == null || this.translatable.isEmpty());
    }

    /**
     * Sets the literal of this Text. Clears the cached {@link Component} on change.
     * 
     * @param literal Literal text.
     * @return This Text object.
     */
    public Text setLiteral(String literal) {
        if(!Objects.equals(this.literal, literal)) {
            this.literal = literal;
            this.cache = null;
        }

        return this;
    }

    /**
     * Sets the translatable (language key) of this Text. Clears the cached {@link
     * Component} on change.
     * 
     * @param translatable Language key.
     * @return This Text object.
     */
    public Text setTranslatable(String translatable) {
        if(!Objects.equals(this.translatable, translatable)) {
            this.translatable = translatable;
            this.cache = null;
        }

        return this;
    }

    /**
     * Clears the cached {@link Component}.
     * 
     * @return This Text object.
     */
    public Text clearCache() {
        this.cache = new Cache();
        return this;
    }

    /**
     * Retrieves the {@link MutableComponent} for this Text. Note that the {@link
     * MutableComponent} will be cached.
     * 
     * @param args Format string args.
     * @return {@link MutableComponent} for this Text.
     * @see Text#clearCache()
     */
    public MutableComponent getComponent(Object... args) {
        if(this.cache == null) {
            this.cache = new Cache();
        }
        
        if(this.cache.component == null || !Arrays.equals(this.cache.args, args)) {
            this.cache.component = this.translatable != null
                ? Component.translatableWithFallback(this.translatable, this.literal == null ? "" : this.literal, args)
                : (this.literal != null ? Component.literal(String.format(this.literal, args)) : Component.empty());
        }

        return this.cache.component;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.literal, this.translatable);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Text o) && Objects.equals(this.literal, o.literal) && Objects.equals(this.translatable, o.translatable);
    }

    @Override
    public int compareTo(Text o) {
        // TODO: can this fail if component requires args?
        return this.getComponent().getString().compareTo(o.getComponent().getString());
    }

    @Override
    public String toString() {
        // TODO: can this fail if component requires args?
        return this.getComponent().getString();
    }

    public static class Deserializer implements JsonDeserializer<Text> {
        @Override
        public Text deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                var t = json.getAsString();
                return Text.literal(t);
            } catch(UnsupportedOperationException e) {
                return GSON.fromJson(json, typeOfT);
            }
        }
    }

    private class Cache {
        public MutableComponent component;
        public Object[] args;
    }
}
