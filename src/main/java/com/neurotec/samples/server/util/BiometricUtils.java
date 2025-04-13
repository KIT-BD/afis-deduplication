package com.neurotec.samples.server.util;


public final class BiometricUtils {
    public static String matchingThresholdToString(int value) {
        double p = -value / 12.0D + 2.0D;
        int decimals = (int) Math.max(0.0D, Math.ceil(-p));
        return String.format("%." + decimals + "f %%", new Object[]{Double.valueOf(Math.pow(10.0D, p))});
    }

    public static int matchingThresholdFromString(String value) {
        double p = Math.log10(Math.max(0.0D, Math.min(1.0D, Double.parseDouble(value.replace("%", "")) / 100.0D)));
        return Math.max(0, (int) Math.round(-12.0D * p));
    }

    public static int maximalRotationToDegrees(int value) {
        return (2 * value * 360 + 256) / 512;
    }

    public static int maximalRotationFromDegrees(int value) {
        return (2 * value * 256 + 360) / 720;
    }
}
