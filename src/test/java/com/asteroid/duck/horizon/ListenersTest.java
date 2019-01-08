package com.asteroid.duck.horizon;

import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class ListenersTest {
    private static class TestListenerImpl implements TestListener {
        public final LinkedList<Integer> historyA = new LinkedList<>();
        public final LinkedList<String> historyB = new LinkedList<>();
        private final Listeners<TestListener> listeners;

        private TestListenerImpl(Listeners<TestListener> listeners) {
            this.listeners = listeners;
        }

        @Override
        public void eventA(int aNumber) {
            historyA.add(aNumber);
        }

        @Override
        public void eventB(String message) {
            historyB.add(message);
            if (message.startsWith("remove")) {
                listeners.remove(this);
            }
        }
    }
    @Test
    public void testSimple() {
        Listeners<TestListener> subject = new Listeners<>();

    }
}