/*
 * Modern UI.
 * Copyright (C) 2019-2023 BloCamLimb. All rights reserved.
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

package icyllis.modernui.graphics.text;

import icyllis.arc3d.core.Strike;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.graphics.Rect;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Locale;

/**
 * Platform abstract font face, may represent a single font file.
 * For standard font, this may represent a fake font or logical font.
 */
public interface Font {

    /**
     * Returns the intrinsic style of this font.
     * For standard fonts, may return the style that this object is simulating.
     * This indicates that this font can directly provide glyph images in this style.
     */
    int getStyle();

    default String getFullName() {
        return getFullName(Locale.ROOT);
    }

    String getFullName(@NonNull Locale locale);

    default String getFamilyName() {
        return getFamilyName(Locale.ROOT);
    }

    String getFamilyName(@NonNull Locale locale);

    /**
     * Expand the font metrics.
     *
     * @param paint a paint object used for retrieving font metrics.
     * @param fm    a nullable destination object. If null is passed, this function only
     *              retrieve recommended interline spacing. If non-null is passed, this function
     *              expands to font metrics to it.
     * @return the font's interline spacing.
     */
    int getMetrics(@NonNull FontPaint paint,
                   @Nullable FontMetricsInt fm);

    boolean hasGlyph(int ch, int vs);

    // map characters to glyphs
    float doSimpleLayout(char[] buf, int start, int limit,
                         FontPaint paint, IntArrayList glyphs,
                         FloatArrayList positions, float x, float y);

    // do HarfBuzz text shaping
    float doComplexLayout(char[] buf,
                          int contextStart, int contextLimit,
                          int layoutStart, int layoutLimit,
                          boolean isRtl, FontPaint paint,
                          IntArrayList glyphs, FloatArrayList positions,
                          float[] advances, int advanceOffset,
                          Rect bounds, float x, float y);

    Strike findOrCreateStrike(FontPaint paint);
}
