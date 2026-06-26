package com.sdrerc.ui.appv2.util;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;

public final class AppV2DisplayScale {

    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    private static final float MIN_SCALE = 0.95f;
    private static final float MAX_SCALE = 1.25f;
    private static volatile Float cachedScale;
    private static volatile Dimension cachedBounds;

    private AppV2DisplayScale() {
    }

    public static Dimension getUsableScreenBounds() {
        Dimension bounds = cachedBounds;
        if (bounds == null) {
            synchronized (AppV2DisplayScale.class) {
                bounds = cachedBounds;
                if (bounds == null) {
                    bounds = resolveUsableBounds();
                    cachedBounds = bounds;
                }
            }
        }
        return new Dimension(bounds);
    }

    public static float getScaleFactor() {
        Float factor = cachedScale;
        if (factor == null) {
            synchronized (AppV2DisplayScale.class) {
                factor = cachedScale;
                if (factor == null) {
                    factor = resolveScaleFactor();
                    cachedScale = factor;
                }
            }
        }
        return factor.floatValue();
    }

    public static int scale(int value) {
        return Math.max(1, Math.round(value * getScaleFactor()));
    }

    public static int scaleFont(int value) {
        return Math.max(11, Math.round(value * getScaleFactor()));
    }

    public static Dimension scaledDimension(int width, int height) {
        return new Dimension(scale(width), scale(height));
    }

    public static Dimension initialWindowSize() {
        Dimension bounds = getUsableScreenBounds();
        int width = Math.min(bounds.width, Math.max(scale(1120), (int) Math.round(bounds.width * 0.94d)));
        int height = Math.min(bounds.height, Math.max(scale(720), (int) Math.round(bounds.height * 0.94d)));
        return new Dimension(width, height);
    }

    public static Dimension minimumWindowSize() {
        return new Dimension(scale(1120), scale(720));
    }

    public static int sidebarExpandedWidth() {
        return scale(304);
    }

    public static int sidebarCollapsedWidth() {
        return scale(72);
    }

    public static boolean isCompactScreen() {
        Dimension bounds = getUsableScreenBounds();
        return bounds.width < 1440 || bounds.height < 820;
    }

    private static float resolveScaleFactor() {
        Dimension bounds = getUsableScreenBounds();
        float widthFactor = bounds.width / (float) BASE_WIDTH;
        float heightFactor = bounds.height / (float) BASE_HEIGHT;
        float factor = Math.min(widthFactor, heightFactor);
        if (Float.isNaN(factor) || Float.isInfinite(factor) || factor <= 0f) {
            factor = 1.0f;
        }
        if (factor < MIN_SCALE) {
            factor = MIN_SCALE;
        }
        if (factor > MAX_SCALE) {
            factor = MAX_SCALE;
        }
        return factor;
    }

    private static Dimension resolveUsableBounds() {
        try {
            return GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds()
                    .getSize();
        }
        catch (HeadlessException ex) {
            return new Dimension(BASE_WIDTH, BASE_HEIGHT);
        }
        catch (Exception ex) {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            return new Dimension(size);
        }
    }
}
