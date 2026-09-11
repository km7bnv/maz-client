package com.maz.client.module;

public class CpsModule extends Module {

    private static final TimestampQueue LEFT_CLICKS = new TimestampQueue();
    private static final TimestampQueue RIGHT_CLICKS = new TimestampQueue();

    public CpsModule() {
        super("CPS", ModuleCategory.COMBAT);
    }

    public static void recordLeftClick() {
        long now = System.currentTimeMillis();
        LEFT_CLICKS.add(now);
        LEFT_CLICKS.prune(now);
    }

    public static void recordRightClick() {
        long now = System.currentTimeMillis();
        RIGHT_CLICKS.add(now);
        RIGHT_CLICKS.prune(now);
    }

    public static int getCps() {
        return getLeftCps();
    }

    public static int getLeftCps() {
        long now = System.currentTimeMillis();
        LEFT_CLICKS.prune(now);
        return LEFT_CLICKS.size();
    }

    public static int getRightCps() {
        long now = System.currentTimeMillis();
        RIGHT_CLICKS.prune(now);
        return RIGHT_CLICKS.size();
    }

    private static final class TimestampQueue {
        private long[] timestamps = new long[32];
        private int head;
        private int size;

        void add(long timestamp) {
            ensureCapacity();
            timestamps[(head + size) % timestamps.length] = timestamp;
            size++;
        }

        void prune(long now) {
            while (size > 0 && now - timestamps[head] > 1000L) {
                head = (head + 1) % timestamps.length;
                size--;
            }
            if (size == 0) {
                head = 0;
            }
        }

        int size() {
            return size;
        }

        private void ensureCapacity() {
            if (size < timestamps.length) {
                return;
            }

            long[] expanded = new long[timestamps.length * 2];
            for (int i = 0; i < size; i++) {
                expanded[i] = timestamps[(head + i) % timestamps.length];
            }
            timestamps = expanded;
            head = 0;
        }
    }
}
