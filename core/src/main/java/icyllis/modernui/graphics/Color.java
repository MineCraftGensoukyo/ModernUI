/*
 * Modern UI.
 * Copyright (C) 2019-2022 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.graphics;

import icyllis.modernui.annotation.ColorInt;

import javax.annotation.Nonnull;

public class Color {

    @ColorInt
    public static final int TRANSPARENT = 0;

    /**
     * Describes different color channels one can manipulate.
     */
    public static final int
            COLOR_CHANNEL_R = 0, // the red channel
            COLOR_CHANNEL_G = 1, // the green channel
            COLOR_CHANNEL_B = 2, // the blue channel
            COLOR_CHANNEL_A = 3; // the alpha channel

    /**
     * Used to represent the channels available in a color type or texture format as a mask.
     */
    public static final int
            COLOR_CHANNEL_FLAG_RED = 1 << COLOR_CHANNEL_R,
            COLOR_CHANNEL_FLAG_GREEN = 1 << COLOR_CHANNEL_G,
            COLOR_CHANNEL_FLAG_BLUE = 1 << COLOR_CHANNEL_B,
            COLOR_CHANNEL_FLAG_ALPHA = 1 << COLOR_CHANNEL_A,
            COLOR_CHANNEL_FLAG_GRAY = 0x10;

    // Convenience values
    public static final int
            COLOR_CHANNEL_FLAGS_RG = COLOR_CHANNEL_FLAG_RED | COLOR_CHANNEL_FLAG_GREEN,
            COLOR_CHANNEL_FLAGS_RGB = COLOR_CHANNEL_FLAGS_RG | COLOR_CHANNEL_FLAG_BLUE,
            COLOR_CHANNEL_FLAGS_RGBA = COLOR_CHANNEL_FLAGS_RGB | COLOR_CHANNEL_FLAG_ALPHA;

    /**
     * Return the alpha component of a color int. This is the same as saying
     * color >>> 24
     */
    public static int alpha(@ColorInt int color) {
        return color >>> 24;
    }

    /**
     * Return the red component of a color int. This is the same as saying
     * (color >> 16) & 0xFF
     */
    public static int red(@ColorInt int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Return the green component of a color int. This is the same as saying
     * (color >> 8) & 0xFF
     */
    public static int green(@ColorInt int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Return the blue component of a color int. This is the same as saying
     * color & 0xFF
     */
    public static int blue(@ColorInt int color) {
        return color & 0xFF;
    }

    /**
     * Return a color-int from red, green, blue components.
     * The alpha component is implicitly 255 (fully opaque).
     * These component values should be \([0..255]\), but there is no
     * range check performed, so if they are out of range, the
     * returned color is undefined.
     *
     * @param red   Red component \([0..255]\) of the color
     * @param green Green component \([0..255]\) of the color
     * @param blue  Blue component \([0..255]\) of the color
     */
    @ColorInt
    public static int rgb(int red, int green, int blue) {
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    /**
     * Return a color-int from red, green, blue float components
     * in the range \([0..1]\). The alpha component is implicitly
     * 1.0 (fully opaque). If the components are out of range, the
     * returned color is undefined.
     *
     * @param red   Red component \([0..1]\) of the color
     * @param green Green component \([0..1]\) of the color
     * @param blue  Blue component \([0..1]\) of the color
     */
    @ColorInt
    public static int rgb(float red, float green, float blue) {
        return 0xFF000000 |
                ((int) (red * 255.0f + 0.5f) << 16) |
                ((int) (green * 255.0f + 0.5f) << 8) |
                (int) (blue * 255.0f + 0.5f);
    }

    /**
     * Return a color-int from alpha, red, green, blue components.
     * These component values should be \([0..255]\), but there is no
     * range check performed, so if they are out of range, the
     * returned color is undefined.
     *
     * @param alpha Alpha component \([0..255]\) of the color
     * @param red   Red component \([0..255]\) of the color
     * @param green Green component \([0..255]\) of the color
     * @param blue  Blue component \([0..255]\) of the color
     */
    @ColorInt
    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Return a color-int from alpha, red, green, blue float components
     * in the range \([0..1]\). If the components are out of range, the
     * returned color is undefined.
     *
     * @param alpha Alpha component \([0..1]\) of the color
     * @param red   Red component \([0..1]\) of the color
     * @param green Green component \([0..1]\) of the color
     * @param blue  Blue component \([0..1]\) of the color
     */
    @ColorInt
    public static int argb(float alpha, float red, float green, float blue) {
        return ((int) (alpha * 255.0f + 0.5f) << 24) |
                ((int) (red * 255.0f + 0.5f) << 16) |
                ((int) (green * 255.0f + 0.5f) << 8) |
                (int) (blue * 255.0f + 0.5f);
    }

    /**
     * </p>Parse the color string, and return the corresponding color-int.
     * If the string cannot be parsed, throws an IllegalArgumentException
     * exception. Supported formats are:</p>
     *
     * <ul>
     *   <li><code>#RRGGBB</code></li>
     *   <li><code>#AARRGGBB</code></li>
     *   <li><code>0xRRGGBB</code></li>
     *   <li><code>0xAARRGGBB</code></li>
     * </ul>
     */
    @ColorInt
    public static int parseColor(@Nonnull String colorString) {
        if (colorString.charAt(0) == '#') {
            int color = Integer.parseUnsignedInt(colorString.substring(1), 16);
            if (colorString.length() == 7) {
                // Set the alpha value
                color |= 0xFF000000;
            } else if (colorString.length() != 9) {
                throw new IllegalArgumentException("Unknown color: " + colorString);
            }
            return color;
        } else if (colorString.startsWith("0x")) { // do not support upper case
            int color = Integer.parseUnsignedInt(colorString.substring(2), 16);
            if (colorString.length() == 8) {
                // Set the alpha value
                color |= 0xFF000000;
            } else if (colorString.length() != 10) {
                throw new IllegalArgumentException("Unknown color: " + colorString);
            }
            return color;
        }
        throw new IllegalArgumentException("Unknown color prefix: " + colorString);
    }

    /**
     * Converts RGB to its HSV components.
     * hsv[0] contains hsv hue, a value from zero to less than 360.
     * hsv[1] contains hsv saturation, a value from zero to one.
     * hsv[2] contains hsv value, a value from zero to one.
     *
     * @param r   red component value from zero to 255
     * @param g   green component value from zero to 255
     * @param b   blue component value from zero to 255
     * @param hsv three element array which holds the resulting HSV components
     */
    public static void RGBToHSV(int r, int g, int b, float[] hsv) {
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));

        int delta = max - min;

        if (delta == 0) {
            hsv[0] = 0;
            hsv[1] = 0;
            hsv[2] = max / 255.0f;
            return;
        }

        float h;

        if (max == r) {
            h = (float) (g - b) / delta;
        } else if (max == g) {
            h = 2.0f + (float) (b - r) / delta;
        } else { // max == blue
            h = 4.0f + (float) (r - g) / delta;
        }

        h *= 60;
        if (h < 0) {
            h += 360;
        }

        hsv[0] = h;
        hsv[1] = (float) delta / max;
        hsv[2] = max / 255.0f;
    }

    /**
     * Converts RGB to its HSV components. Alpha in ARGB (if it has) is ignored.
     * hsv[0] contains hsv hue, and is assigned a value from zero to less than 360.
     * hsv[1] contains hsv saturation, a value from zero to one.
     * hsv[2] contains hsv value, a value from zero to one.
     *
     * @param color RGB or ARGB color to convert
     * @param hsv   three element array which holds the resulting HSV components
     */
    public static void RGBToHSV(int color, float[] hsv) {
        RGBToHSV(red(color), green(color), blue(color), hsv);
    }

    /**
     * Converts HSV components to an RGB color. Alpha is NOT implicitly set.
     * <p>
     * Out of range hsv values are clamped.
     *
     * @param h hsv hue, an angle from zero to less than 360
     * @param s hsv saturation, and varies from zero to one
     * @param v hsv value, and varies from zero to one
     * @return RGB equivalent to HSV, without alpha
     */
    public static int HSVToColor(float h, float s, float v) {
        s = MathUtil.clamp(s, 0.0f, 1.0f);
        v = MathUtil.clamp(v, 0.0f, 1.0f);

        if (s <= 1.0f / 1024.0f) {
            int i = (int) (v * 255.0f + 0.5f);
            return (i << 16) | (i << 8) | i;
        }

        float hx = (h < 0 || h >= 360.0f) ? 0 : h / 60.0f;
        int w = (int) hx;
        float f = hx - w;

        float p = v * (1.0f - s);
        float q = v * (1.0f - (s * f));
        float t = v * (1.0f - (s * (1.0f - f)));

        float r, g, b;

        switch (w) {
            case 0 -> {
                r = v;
                g = t;
                b = p;
            }
            case 1 -> {
                r = q;
                g = v;
                b = p;
            }
            case 2 -> {
                r = p;
                g = v;
                b = t;
            }
            case 3 -> {
                r = p;
                g = q;
                b = v;
            }
            case 4 -> {
                r = t;
                g = p;
                b = v;
            }
            default -> {
                r = v;
                g = p;
                b = q;
            }
        }
        return ((int) (r * 255.0f + 0.5f) << 16) |
                ((int) (g * 255.0f + 0.5f) << 8) |
                (int) (b * 255.0f + 0.5f);
    }

    /**
     * Converts HSV components to an RGB color. Alpha is NOT implicitly set.
     * hsv[0] represents hsv hue, an angle from zero to less than 360.
     * hsv[1] represents hsv saturation, and varies from zero to one.
     * hsv[2] represents hsv value, and varies from zero to one.
     * <p>
     * Out of range hsv values are clamped.
     *
     * @param hsv three element array which holds the input HSV components
     * @return RGB equivalent to HSV, without alpha
     */
    public static int HSVToColor(float[] hsv) {
        return HSVToColor(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * Converts a color component from the sRGB space to the linear RGB space,
     * using the sRGB transfer function.
     *
     * @param x a color component
     * @return transformed color component
     */
    public static float GammaToLinear(float x) {
        return x < 0.04045
                ? x / 12.92f
                : (float) Math.pow((x + 0.055) / 1.055, 2.4);
    }

    /**
     * Converts a color component from the linear RGB space to the sRGB space,
     * using the inverse of sRGB transfer function.
     *
     * @param x a color component
     * @return transformed color component
     */
    public static float LinearToGamma(float x) {
        return x < 0.04045 / 12.92
                ? x * 12.92f
                : (float) Math.pow(x, 1.0 / 2.4) * 1.055f - 0.055f;
    }

    /**
     * Converts a color from the sRGB space to the linear RGB space,
     * using the sRGB transfer function.
     *
     * @param col the color components
     */
    public static void GammaToLinear(float[] col) {
        col[0] = GammaToLinear(col[0]);
        col[1] = GammaToLinear(col[1]);
        col[2] = GammaToLinear(col[2]);
    }

    /**
     * Converts a color from the linear RGB space to the sRGB space,
     * using the inverse of sRGB transfer function.
     *
     * @param col the color components
     */
    public static void LinearToGamma(float[] col) {
        col[0] = LinearToGamma(col[0]);
        col[1] = LinearToGamma(col[1]);
        col[2] = LinearToGamma(col[2]);
    }

    /**
     * Converts a linear RGB color to a luminance value.
     */
    public static float luminance(float r, float g, float b) {
        return 0.2126f * r + 0.7152f * g + 0.0722f * b;
    }

    /**
     * Converts a linear RGB color to a luminance value.
     *
     * @param col the color components
     */
    public static float luminance(float[] col) {
        return luminance(col[0], col[1], col[2]);
    }

    /**
     * Coverts a luminance value to a perceptual lightness value.
     */
    public static float lightness(float lum) {
        return lum <= 216.0 / 24389.0
                ? lum * 24389.0f / 27.0f
                : (float) Math.pow(lum, 1.0 / 3.0) * 116.0f - 16.0f;
    }

    /**
     * Blends the two colors using premultiplied alpha on CPU side. This is to simulate
     * the color blending on GPU side, but this is only used for color filtering (tinting).
     * Do NOT premultiply the src and dst colors with alpha on CPU side. The returned
     * color is un-premultiplied by alpha. This method will not lose precision,
     * color components are still 8-bit.
     *
     * @param mode the blend mode that determines blending factors
     * @param src  the source color (straight) to be blended into the destination color
     * @param dst  the destination color (straight) on which the source color is to be blended
     * @return the color (straight) resulting from the color blending
     */
    @ColorInt
    public static int blend(@Nonnull BlendMode mode, @ColorInt int src, @ColorInt int dst) {
        return switch (mode) {
            case CLEAR -> TRANSPARENT;
            case SRC -> src;
            case DST -> dst;
            case SRC_OVER -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0xFF)
                    yield src;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0)
                    yield src;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float srcR = red(src) / 255.0f * srcA;
                float srcG = green(src) / 255.0f * srcA;
                float srcB = blue(src) / 255.0f * srcA;
                float dstA = dstAlpha / 255.0f;
                float dstR = red(dst) / 255.0f * dstA;
                float dstG = green(dst) / 255.0f * dstA;
                float dstB = blue(dst) / 255.0f * dstA;
                // blend
                float oneMinusSrcA = 1.0f - srcA;
                float outA = srcA + oneMinusSrcA * dstA;
                if (outA == 0.0f)
                    yield TRANSPARENT;
                float outR = srcR + oneMinusSrcA * dstR;
                float outG = srcG + oneMinusSrcA * dstG;
                float outB = srcB + oneMinusSrcA * dstB;
                // un-premultiply the out color
                float invA = 1.0f / outA;
                yield argb(outA, outR * invA, outG * invA, outB * invA);
            }
            case DST_OVER -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0)
                    yield dst;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0xFF)
                    yield dst;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float srcR = red(src) / 255.0f * srcA;
                float srcG = green(src) / 255.0f * srcA;
                float srcB = blue(src) / 255.0f * srcA;
                float dstA = dstAlpha / 255.0f;
                float dstR = red(dst) / 255.0f * dstA;
                float dstG = green(dst) / 255.0f * dstA;
                float dstB = blue(dst) / 255.0f * dstA;
                // blend
                float oneMinusDstA = 1.0f - dstA;
                float outA = dstA + oneMinusDstA * srcA;
                if (outA == 0.0f)
                    yield TRANSPARENT;
                float outR = dstR + oneMinusDstA * srcR;
                float outG = dstG + oneMinusDstA * srcG;
                float outB = dstB + oneMinusDstA * srcB;
                // un-premultiply the out color
                float invA = 1.0f / outA;
                yield argb(outA, outR * invA, outG * invA, outB * invA);
            }
            case SRC_IN -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0)
                    yield TRANSPARENT;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0xFF)
                    yield src;
                if (dstAlpha == 0)
                    yield TRANSPARENT;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float srcR = red(src) / 255.0f * srcA;
                float srcG = green(src) / 255.0f * srcA;
                float srcB = blue(src) / 255.0f * srcA;
                float dstA = dstAlpha / 255.0f;
                // blend
                float outA = srcA * dstA;
                if (outA == 0.0f)
                    yield TRANSPARENT;
                float outR = srcR * dstA;
                float outG = srcG * dstA;
                float outB = srcB * dstA;
                // un-premultiply the out color
                float invA = 1.0f / outA;
                yield argb(outA, outR * invA, outG * invA, outB * invA);
            }
            case DST_IN -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0xFF)
                    yield dst;
                if (srcAlpha == 0)
                    yield TRANSPARENT;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0)
                    yield TRANSPARENT;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float dstA = dstAlpha / 255.0f;
                float dstR = red(dst) / 255.0f * dstA;
                float dstG = green(dst) / 255.0f * dstA;
                float dstB = blue(dst) / 255.0f * dstA;
                // blend
                float outA = dstA * srcA;
                if (outA == 0.0f)
                    yield TRANSPARENT;
                float outR = dstR * srcA;
                float outG = dstG * srcA;
                float outB = dstB * srcA;
                // un-premultiply the out color
                float invA = 1.0f / outA;
                yield argb(outA, outR * invA, outG * invA, outB * invA);
            }
            case SRC_OUT -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0)
                    yield TRANSPARENT;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0)
                    yield src;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float srcR = red(src) / 255.0f * srcA;
                float srcG = green(src) / 255.0f * srcA;
                float srcB = blue(src) / 255.0f * srcA;
                float dstA = dstAlpha / 255.0f;
                // blend
                float oneMinusDstA = 1.0f - dstA;
                float outA = srcA * oneMinusDstA;
                if (outA == 0.0f)
                    yield TRANSPARENT;
                float outR = srcR * oneMinusDstA;
                float outG = srcG * oneMinusDstA;
                float outB = srcB * oneMinusDstA;
                // un-premultiply the out color
                float invA = 1.0f / outA;
                yield argb(outA, outR * invA, outG * invA, outB * invA);
            }
            case DST_OUT -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0)
                    yield dst;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0)
                    yield TRANSPARENT;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float dstA = dstAlpha / 255.0f;
                float dstR = red(dst) / 255.0f * dstA;
                float dstG = green(dst) / 255.0f * dstA;
                float dstB = blue(dst) / 255.0f * dstA;
                // blend
                float oneMinusSrcA = 1.0f - srcA;
                float outA = srcA * oneMinusSrcA;
                if (outA == 0.0f)
                    yield TRANSPARENT;
                float outR = dstR * oneMinusSrcA;
                float outG = dstG * oneMinusSrcA;
                float outB = dstB * oneMinusSrcA;
                // un-premultiply the out color
                float invA = 1.0f / outA;
                yield argb(outA, outR * invA, outG * invA, outB * invA);
            }
            case SRC_ATOP -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0)
                    yield dst;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0)
                    yield TRANSPARENT;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float srcR = red(src) / 255.0f * srcA;
                float srcG = green(src) / 255.0f * srcA;
                float srcB = blue(src) / 255.0f * srcA;
                float dstA = dstAlpha / 255.0f;
                float dstR = red(dst) / 255.0f * dstA;
                float dstG = green(dst) / 255.0f * dstA;
                float dstB = blue(dst) / 255.0f * dstA;
                // blend
                float oneMinusSrcA = 1.0f - srcA;
                float outR = srcR * dstA + dstR * oneMinusSrcA;
                float outG = srcG * dstA + dstG * oneMinusSrcA;
                float outB = srcB * dstA + dstB * oneMinusSrcA;
                // un-premultiply the out color
                float invA = 1.0f / dstA;
                yield argb(dstA, outR * invA, outG * invA, outB * invA);
            }
            case DST_ATOP -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0)
                    yield TRANSPARENT;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0)
                    yield src;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float srcR = red(src) / 255.0f * srcA;
                float srcG = green(src) / 255.0f * srcA;
                float srcB = blue(src) / 255.0f * srcA;
                float dstA = dstAlpha / 255.0f;
                float dstR = red(dst) / 255.0f * dstA;
                float dstG = green(dst) / 255.0f * dstA;
                float dstB = blue(dst) / 255.0f * dstA;
                // blend
                float oneMinusDstA = 1.0f - dstA;
                float outR = dstR * srcA + srcR * oneMinusDstA;
                float outG = dstG * srcA + srcG * oneMinusDstA;
                float outB = dstB * srcA + srcB * oneMinusDstA;
                // un-premultiply the out color
                float invA = 1.0f / srcA;
                yield argb(srcA, outR * invA, outG * invA, outB * invA);
            }
            case XOR -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0)
                    yield dst;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0)
                    yield src;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float srcR = red(src) / 255.0f * srcA;
                float srcG = green(src) / 255.0f * srcA;
                float srcB = blue(src) / 255.0f * srcA;
                float dstA = dstAlpha / 255.0f;
                float dstR = red(dst) / 255.0f * dstA;
                float dstG = green(dst) / 255.0f * dstA;
                float dstB = blue(dst) / 255.0f * dstA;
                // blend
                float oneMinusSrcA = 1.0f - srcA;
                float oneMinusDstA = 1.0f - dstA;
                float outA = srcA * oneMinusDstA + dstA * oneMinusSrcA;
                if (outA == 0.0f)
                    yield TRANSPARENT;
                float outR = srcR * oneMinusDstA + dstR * oneMinusSrcA;
                float outG = srcG * oneMinusDstA + dstG * oneMinusSrcA;
                float outB = srcB * oneMinusDstA + dstB * oneMinusSrcA;
                // un-premultiply the out color
                float invA = 1.0f / outA;
                yield argb(outA, outR * invA, outG * invA, outB * invA);
            }
            case PLUS -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0)
                    yield dst;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0)
                    yield src;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float srcR = red(src) / 255.0f * srcA;
                float srcG = green(src) / 255.0f * srcA;
                float srcB = blue(src) / 255.0f * srcA;
                float dstA = dstAlpha / 255.0f;
                float dstR = red(dst) / 255.0f * dstA;
                float dstG = green(dst) / 255.0f * dstA;
                float dstB = blue(dst) / 255.0f * dstA;
                // blend
                float outA = Math.min(srcA + dstA, 1);
                if (outA == 0.0f)
                    yield TRANSPARENT;
                float outR = Math.min(srcR + dstR, 1);
                float outG = Math.min(srcG + dstG, 1);
                float outB = Math.min(srcB + dstB, 1);
                // un-premultiply the out color
                float invA = 1.0f / outA;
                yield argb(outA, outR * invA, outG * invA, outB * invA);
            }
            case MULTIPLY -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0)
                    yield TRANSPARENT;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0)
                    yield TRANSPARENT;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float srcR = red(src) / 255.0f * srcA;
                float srcG = green(src) / 255.0f * srcA;
                float srcB = blue(src) / 255.0f * srcA;
                float dstA = dstAlpha / 255.0f;
                float dstR = red(dst) / 255.0f * dstA;
                float dstG = green(dst) / 255.0f * dstA;
                float dstB = blue(dst) / 255.0f * dstA;
                // blend
                float outA = srcA * dstA;
                if (outA == 0.0f)
                    yield TRANSPARENT;
                float outR = srcR * dstR;
                float outG = srcG * dstG;
                float outB = srcB * dstB;
                // un-premultiply the out color
                float invA = 1.0f / outA;
                yield argb(outA, outR * invA, outG * invA, outB * invA);
            }
            case SCREEN -> {
                int srcAlpha = alpha(src);
                if (srcAlpha == 0)
                    yield dst;
                int dstAlpha = alpha(dst);
                if (dstAlpha == 0)
                    yield src;
                // premultiply the src and dst colors
                float srcA = srcAlpha / 255.0f;
                float srcR = red(src) / 255.0f * srcA;
                float srcG = green(src) / 255.0f * srcA;
                float srcB = blue(src) / 255.0f * srcA;
                float dstA = dstAlpha / 255.0f;
                float dstR = red(dst) / 255.0f * dstA;
                float dstG = green(dst) / 255.0f * dstA;
                float dstB = blue(dst) / 255.0f * dstA;
                // blend
                float outA = srcA + dstA - srcA * dstA;
                if (outA == 0.0f)
                    yield TRANSPARENT;
                float outR = srcR + dstR - srcR * dstR;
                float outG = srcG + dstG - srcG * dstG;
                float outB = srcB + dstB - srcB * dstB;
                // un-premultiply the out color
                float invA = 1.0f / outA;
                yield argb(outA, outR * invA, outG * invA, outB * invA);
            }
            default -> src;
        };
    }
}
