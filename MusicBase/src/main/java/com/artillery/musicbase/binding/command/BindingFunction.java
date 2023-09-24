package com.artillery.musicbase.binding.command;

/**
 * Represents a function with zero arguments.
 *
 * @param <T> the result type
 * @author ArtilleryOrchid
 */
public interface BindingFunction<T> {
    T call();
}
