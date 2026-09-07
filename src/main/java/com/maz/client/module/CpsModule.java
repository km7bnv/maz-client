package com.maz.client.module;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsModule extends Module {

    private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();

    public CpsModule() {
        super("CPS", ModuleCategory.COMBAT);
    }

    public static void recordLeftClick() {
        long now = System.currentTimeMillis();
        LEFT_CLICKS.addLast(now);
        prune(now);
    }

    public static void recordRightClick() {
        long now = System.currentTimeMillis();
        RIGHT_CLICKS.addLast(now);
        prune(now);
    }

    public static int getCps() {
        return getLeftCps();
    }

    public static int getLeftCps() {
        long now = System.currentTimeMillis();
        prune(now);
        return LEFT_CLICKS.size();
    }

    public static int getRightCps() {
        long now = System.currentTimeMillis();
        prune(now);
        return RIGHT_CLICKS.size();
    }

    private static void prune(long now) {
        while (!LEFT_CLICKS.isEmpty()
                && now - LEFT_CLICKS.peekFirst() > 1000L) {
            LEFT_CLICKS.removeFirst();
        }
        while (!RIGHT_CLICKS.isEmpty()
                && now - RIGHT_CLICKS.peekFirst() > 1000L) {
            RIGHT_CLICKS.removeFirst();
        }
    }
}
