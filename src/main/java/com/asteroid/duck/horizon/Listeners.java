package com.asteroid.duck.horizon;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages a list of listeners for the purpose of publishing events to those listeners
 */
public class Listeners<T> {
    /**
     * A list of listeners to notify
     */
    private final ArrayList<T> listeners = new ArrayList<>(2);
    /**
     * A lock for safe concurrent access to the listener list
     */
    private final ReentrantLock listenersLock = new ReentrantLock(false);

    /**
     * Add the listener to the list
     * @param listener the listener to add (null ignored)
     */
    public void add(T listener) {
        if (listener != null) {
            listenersLock.lock();
            try {
                listeners.add(listener);
            } finally {
                listenersLock.unlock();
            }
        }
    }

    public int size() {
        return listeners.size();
    }

    /**
     * Remove a listener from the list
     * @param listener the listener to remove (null ignored)
     */
    public void remove(T listener) {
        if (listener != null) {
            listenersLock.lock();
            try {
                listeners.remove(listener);
            } finally {
                listenersLock.unlock();
            }
        }
    }

    /**
     * Use a notifier to notify all current listeners of an event.
     * This makes a copy of the listener list prior to iteration - so changes by listeners (or other threads)
     * during iteration will have no effect.
     * @param notifier the notifier which knows how to notify each individual listener
     */
    public void notify(Notifier<T> notifier) {
        if (notifier != null) {
            T[] clone = null;
            listenersLock.lock();
            try {
                clone = (T[]) listeners.toArray();
            } finally {
                listenersLock.unlock();
            }
            if (clone != null) {
                for(T listener : clone) {
                    notifier.notify(listener);
                }
            }
        }
    }
}
