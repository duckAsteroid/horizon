package com.asteroid.duck.horizon;

/**
 * Interface to a class that will notify an individual listener
 * of some event
 * @param <T> the listener type
 */
public interface Notifier<T> {
    /**
     * The implementation should notify the provided listener of the event
     * @param listener the listener to notify
     */
    void notify(T listener);
}
