package com.asteroid.duck.horizon;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class ListenersTest {
    private static final DateFormat FMT = new SimpleDateFormat("kk:mm:ss.SSS");

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

    private static class TestNotifier implements Notifier<TestListener> {
        private final Boolean aOrB;
        public static final Boolean NO_EVENT = null;
        public static final Boolean A = true;
        public static final Boolean B = false;

        public int count;
        public String message = "Default message";

        private TestNotifier(Boolean aOrB) {
            this.aOrB = aOrB;
        }

        @Override
        public void notify(TestListener listener) {
            assertNotNull(listener);
            count++;
            if (aOrB != null) {
                if(aOrB) {
                    listener.eventA(count);
                }
                else {
                    listener.eventB(message);
                }
            }
        }
    }

    @Test
    public void testSimple() {
        Listeners<TestListener> subject = new Listeners<>();
        assertEquals(0, subject.size());

        TestListenerImpl l1 = new TestListenerImpl(subject);
        subject.add(l1);
        assertEquals(1, subject.size());

        TestListenerImpl l2 = new TestListenerImpl(subject);
        subject.add(l2);
        assertEquals(2, subject.size());

        subject.add(null);
        assertEquals(2, subject.size());

        TestNotifier notifier = new TestNotifier(TestNotifier.NO_EVENT);
        subject.notify(notifier);
        assertEquals(2, notifier.count);

        subject.remove(null);
        assertEquals(2, subject.size());

        notifier = new TestNotifier(TestNotifier.NO_EVENT);
        subject.notify(notifier);
        assertEquals(2, notifier.count);

        // now do a removal mid iteration
        notifier = new TestNotifier(TestNotifier.B);
        notifier.message = "remove";
        subject.notify(notifier);
        assertEquals(2, notifier.count);

        assertEquals(0, subject.size());

        assertEquals(l1.historyA, l2.historyA);
        assertEquals(l1.historyB, l2.historyB);
    }

    @Test
    public void testConcurrent() throws InterruptedException, ExecutionException {
        Listeners<TestListener> subject = new Listeners<>();
        TestListenerImpl l1 = new TestListenerImpl(subject);
        TestListenerImpl l2 = new TestListenerImpl(subject);

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Random rnd = new Random();
        Callable<Object>[] tasks = new Callable[] {
                newTask(rnd, subject, l1, l2),
                newTask(rnd, subject, l1, l2),
                newTask(rnd, subject, l1, l2),
                newTask(rnd, subject, l1, l2),
                newTask(rnd, subject, l1, l2),
                newTask(rnd, subject, l1, l2),
                newTask(rnd, subject, l1, l2),
                newTask(rnd, subject, l1, l2),
        };

        List<Future<Object>> futures = executorService.invokeAll(Arrays.asList(tasks));
        for(Future<Object> f : futures) {
            f.get();
        }
        executorService.shutdown();

        System.out.println(Arrays.toString(l1.historyB.toArray()));
        System.out.println(Arrays.toString(l2.historyB.toArray()));
    }

    private static Callable<Object> newTask(final Random rnd, Listeners<TestListener> subject, TestListener l1, TestListener l2) {

        return new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                TestListener listener = rnd.nextInt(2) == 0 ? l1 : l2;
                Thread.sleep(rnd.nextInt(1000));
                subject.add(listener);
                Thread.sleep(rnd.nextInt(1000));
                Date now = new Date();
                String tstamp = FMT.format(now);
                subject.notify(notifyMe -> {
                    int anInt = rnd.nextInt(10);
                    String msg = anInt <= 1 ? "remove@" + tstamp : tstamp ;
                    notifyMe.eventB(msg);
                    System.out.println(Thread.currentThread().getName());
                });
                Thread.sleep(rnd.nextInt(1000));
                subject.remove(listener);
                return listener;
            }
        };
    }
}