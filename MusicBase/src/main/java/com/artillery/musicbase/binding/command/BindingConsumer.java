package com.artillery.musicbase.binding.command;

/**
 * A one-argument action.
 *
 * @author ArtilleryOrchid
 * @param <T> the first argument type
 */
public interface BindingConsumer<T> {
    void call(T t);
}
