package com.maz.client.module;

public final class CombatStats {

    private static int combo;
    private static long lastHitMillis;
    private static double lastReach;

    private CombatStats() {
    }

    public static void recordHit(double reach) {
        long now = System.currentTimeMillis();
        if (now - lastHitMillis > 2000L) {
            combo = 0;
        }
        combo++;
        lastHitMillis = now;
        lastReach = Math.max(0.0, reach);
    }

    public static int getCombo() {
        if (System.currentTimeMillis() - lastHitMillis > 2000L) {
            combo = 0;
        }
        return combo;
    }

    public static double getLastReach() {
        return lastReach;
    }
}
