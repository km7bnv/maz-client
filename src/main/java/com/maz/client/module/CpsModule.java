package com.maz.client.module;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsModule extends Module {

    private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();

    public CpsModule() {
        super("CPS", ModuleCategory.HUD);
    }

    public static void recordLeftClick() {
        long now = System.currentTimeMillis();
        LEFT_CLICKS.addLast(now);
        prune(now);
    }

    public static int getCps() {
        long now = System.currentTimeMillis();
        prune(now);
        return LEFT_CLICKS.size();
    }

    private static void prune(long now) {
        while (!LEFT_CLICKS.isEmpty()
                && now - LEFT_CLICKS.peekFirst() > 1000L) {
            LEFT_CLICKS.removeFirst();
        }
    }
}
