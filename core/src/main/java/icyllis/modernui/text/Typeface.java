/*
 * Modern UI.
 * Copyright (C) 2019-2021 BloCamLimb. All rights reserved.
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

package icyllis.modernui.text;

import com.ibm.icu.impl.UCharacterProperty;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterCategory;
import icyllis.modernui.ModernUI;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

/**
 * The FontCollection specifies a set of font families that can be used
 * in Paint. This determines how text appears when drawn and measured.
 * <p>
 * The static part of this class is managing all fonts used in Modern UI.
 */
@Immutable
public class Typeface {

    // log marker
    public static final Marker MARKER = MarkerManager.getMarker("Font");

    @Nonnull
    public static final Typeface SANS_SERIF;

    @Nonnull
    public static final Typeface SERIF;

    @Nonnull
    public static final Typeface MONOSPACED;

    @Nonnull
    public static final Typeface INTERNAL;

    @Nonnull
    private static final Font sSansSerifFont;

    private static final List<String> sFontFamilyNames;

    // internal use
    public static final List<Font> sAllFontFamilies = new ArrayList<>();
    static final Map<String, Typeface> sSystemFontMap = new HashMap<>();

    static {
        //checkJava();
        // Use Java's logical font as the default initial font if user does not override it in some configuration file
        GraphicsEnvironment.getLocalGraphicsEnvironment().preferLocaleFonts();

        List<Font> p = new ArrayList<>();

        try (InputStream stream = new FileInputStream("F:/Torus Regular.otf")) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            p.add(font);
        } catch (FontFormatException | IOException ignored) {
        }

        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.ROOT);
        Font sansSerif = null;
        for (String family : families) {
            Font font = new Font(family, Font.PLAIN, 1);
            sAllFontFamilies.add(font);
            if (family.equals(Font.SANS_SERIF)) {
                sansSerif = font;
            } else if (p.isEmpty() && family.startsWith("SimHei")) {
                p.add(font);
            }
        }
        if (sansSerif == null) {
            sansSerif = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
            ModernUI.LOGGER.warn(MARKER, "Sans Serif is missing?");
        }
        sSansSerifFont = sansSerif;

        p.add(sansSerif);
        INTERNAL = new Typeface(p.toArray(new Font[0]));

        sFontFamilyNames = List.of(families);

        for (Font font : sAllFontFamilies) {
            String family = font.getFamily(Locale.ROOT);
            if (family.equals(Font.SANS_SERIF)) {
                continue;
            }
            Typeface typeface = new Typeface(new Font[]{font, sansSerif});
            typeface = sSystemFontMap.putIfAbsent(family, typeface);
            if (typeface != null) {
                ModernUI.LOGGER.warn(MARKER, "Ignoring duplicated font family: {}", family);
            }
        }

        // no backup strategy
        SANS_SERIF = new Typeface(new Font[]{sansSerif});
        sSystemFontMap.put(Font.SANS_SERIF, SANS_SERIF);

        Typeface serif = sSystemFontMap.get(Font.SERIF);
        if (serif == null) {
            serif = new Typeface(new Font[]{new Font(Font.SERIF, Font.PLAIN, 1)});
            sSystemFontMap.put(Font.SERIF, serif);
        }
        SERIF = serif;

        Typeface monospaced = sSystemFontMap.get(Font.MONOSPACED);
        if (monospaced == null) {
            monospaced = new Typeface(new Font[]{new Font(Font.MONOSPACED, Font.PLAIN, 1)});
            sSystemFontMap.put(Font.MONOSPACED, monospaced);
        }
        MONOSPACED = monospaced;
    }

    /*@Deprecated
    private static void checkJava() {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion == null) {
            ModernUI.LOGGER.fatal(ModernUI.MARKER, "Java version is missing");
        } else {
            try {
                int majorNumber = Integer.parseInt(javaVersion.split("\\.")[0]);
                if (majorNumber < 11 && LocalStorage.checkOneTimeEvent(1)) {
                    ModernUI.get().warnSetup("warning.modernui.old_java", "11.0.9", javaVersion);
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                ModernUI.LOGGER.warn(GlyphManager.MARKER, "Failed to check major java version: {}", javaVersion, e);
            }
            if (javaVersion.startsWith("1.8")) {
                try {
                    int update = Integer.parseInt(javaVersion.split("_")[1].split("-")[0]);
                    if (update < 201) {
                        sJavaTooOld = true;
                        ModernUI.LOGGER.warn(GlyphManager.MARKER, "Java {} is too old to use external fonts",
                        javaVersion);
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    ModernUI.LOGGER.warn(ModernUI.MARKER, "Failed to check java version update: {}", javaVersion, e);
                }
            }
        }
    }*/

    @Nonnull
    public static Typeface createTypeface(@Nonnull Font[] fonts) {
        if (fonts.length == 0) {
            return SANS_SERIF;
        }
        return new Typeface(fonts);
    }

    // unmodifiable
    public static List<String> getFontFamilyNames() {
        return sFontFamilyNames;
    }

    @Nonnull
    public static Typeface getSystemFont(@Nonnull String familyName) {
        Typeface t = sSystemFontMap.get(familyName);
        return t == null ? SANS_SERIF : t;
    }

    // 0b0000 0000 0000 0000 0000 0001 1100 0000
    public static final int GC_M_MASK =
            UCharacterProperty.getMask(UCharacterCategory.COMBINING_SPACING_MARK) |
                    UCharacterProperty.getMask(UCharacterCategory.ENCLOSING_MARK) |
                    UCharacterProperty.getMask(UCharacterCategory.NON_SPACING_MARK);

    // Characters where we want to continue using existing font run for (or stick to the next run if
    // they start a string), even if the font does not support them explicitly. These are handled
    // properly by HarfBuzz (JDK11+) even if the font does not explicitly support them and it's
    // usually meaningless to switch to a different font to display them.
    public static boolean doesNotNeedFontSupport(int c) {
        return c == 0x00AD                       // SOFT HYPHEN
                || c == 0x034F                   // COMBINING GRAPHEME JOINER
                || c == 0x061C                   // ARABIC LETTER MARK
                || (0x200C <= c && c <= 0x200F)  // ZERO WIDTH NON-JOINER..RIGHT-TO-LEFT MARK
                || (0x202A <= c && c <= 0x202E)  // LEFT-TO-RIGHT EMBEDDING..RIGHT-TO-LEFT OVERRIDE
                || (0x2066 <= c && c <= 0x2069)  // LEFT-TO-RIGHT ISOLATE..POP DIRECTIONAL ISOLATE
                || c == 0xFEFF                   // BYTE ORDER MARK
                || isVariationSelector(c);
    }

    public static final int REPLACEMENT_CHARACTER = 0xFFFD;

    // Characters where we want to continue using existing font run instead of
    // recomputing the best match in the fallback list.
    private static final int[] sStickyWhitelist = {
            '!', ',', '-', '.', ':', ';', '?',
            0x00A0,  // NBSP
            0x2010,  // HYPHEN
            0x2011,  // NB_HYPHEN
            0x202F,  // NNBSP
            0x2640,  // FEMALE_SIGN,
            0x2642,  // MALE_SIGN,
            0x2695,  // STAFF_OF_AESCULAPIUS
    };

    public static boolean isStickyWhitelisted(int c) {
        for (int value : sStickyWhitelist)
            if (value == c)
                return true;
        return false;
    }

    public static boolean isCombining(int c) {
        return (UCharacterProperty.getMask(UCharacter.getType(c)) & GC_M_MASK) != 0;
    }

    public static boolean isVariationSelector(int c) {
        return (c >= 0xE0100 && c <= 0xE01FF) || (c >= 0xFE00 && c <= 0xFE0F);
    }

    // an array of base fonts
    @Nonnull
    private final List<Font> mFonts;

    private Typeface(@Nonnull Font[] fonts) {
        if (fonts.length == 0) {
            throw new IllegalArgumentException("Font set cannot be empty");
        }
        mFonts = List.of(fonts);
    }

    // calculate font runs
    public List<FontRun> itemize(@Nonnull final char[] text, final int offset, final int limit) {
        if (offset < 0 || offset > limit || limit > text.length) {
            throw new IllegalArgumentException();
        }
        if (offset == limit) {
            return Collections.emptyList();
        }

        final List<FontRun> result = new ArrayList<>();

        FontRun lastRun = null;
        Font lastFamily = null;

        int nextCh;
        int prevCh = 0;
        int next = offset;
        int index = offset;

        char _c1 = text[index];
        char _c2;
        if (Character.isHighSurrogate(_c1) && index + 1 < limit) {
            _c2 = text[index + 1];
            if (Character.isLowSurrogate(_c2)) {
                nextCh = Character.toCodePoint(_c1, _c2);
                ++index;
            } else if (Character.isSurrogate(_c1)) {
                nextCh = REPLACEMENT_CHARACTER;
            } else {
                nextCh = _c1;
            }
        } else if (Character.isSurrogate(_c1)) {
            nextCh = REPLACEMENT_CHARACTER;
        } else {
            nextCh = _c1;
        }
        ++index;

        boolean running = true;
        do {
            int ch = nextCh;
            int pos = next;
            next = index;

            if (index < limit) {
                _c1 = text[index];
                if (Character.isHighSurrogate(_c1) && index + 1 < limit) {
                    _c2 = text[index + 1];
                    if (Character.isLowSurrogate(_c2)) {
                        nextCh = Character.toCodePoint(_c1, _c2);
                        ++index;
                    } else if (Character.isSurrogate(_c1)) {
                        nextCh = REPLACEMENT_CHARACTER;
                    } else {
                        nextCh = _c1;
                    }
                } else if (Character.isSurrogate(_c1)) {
                    nextCh = REPLACEMENT_CHARACTER;
                } else {
                    nextCh = _c1;
                }
                ++index;
            } else {
                running = false;
            }

            boolean shouldContinueRun = false;
            if (doesNotNeedFontSupport(ch)) {
                // Always continue if the character is a format character not needed to be in the font.
                shouldContinueRun = true;
            } else if (lastFamily != null && (isStickyWhitelisted(ch) || isCombining(ch))) {
                // Continue using existing font as long as it has coverage and is whitelisted.
                shouldContinueRun = lastFamily.canDisplay(ch);
            }

            if (!shouldContinueRun) {
                Font family = getFamilyForChar(ch);
                if (pos == 0 || family != lastFamily) {
                    int start = pos;
                    // Workaround for combining marks and emoji modifiers until we implement
                    // per-cluster font selection: if a combining mark or an emoji modifier is found in
                    // a different font that also supports the previous character, attach previous
                    // character to the new run. U+20E3 COMBINING ENCLOSING KEYCAP, used in emoji, is
                    // handled properly by this since it's a combining mark too.
                    if (pos != 0 &&
                            (isCombining(ch) || (Emoji.isEmojiModifier(ch) && Emoji.isEmojiBase(prevCh)))) {
                        int prevLength = Character.charCount(prevCh);
                        if (lastRun != null) {
                            lastRun.mEnd -= prevLength;
                            if (lastRun.mStart == lastRun.mEnd) {
                                result.remove(lastRun);
                            }
                        }
                        start -= prevLength;
                    }
                    if (lastFamily == null) {
                        // This is the first family ever assigned. We are either seeing the very first
                        // character (which means start would already be zero), or we have only seen
                        // characters that don't need any font support (which means we need to adjust
                        // start to be 0 to include those characters).
                        start = offset;
                    }
                    FontRun run = new FontRun(family, start, 0);
                    result.add(run);
                    lastRun = run;
                    lastFamily = family;
                }
            }
            prevCh = ch;
            if (lastRun != null) {
                lastRun.mEnd = next;
            }

        } while (running);

        if (lastFamily == null) {
            // No character needed any font support, so it doesn't really matter which font they end up
            // getting displayed in. We put the whole string in one run, using the first font.
            result.add(new FontRun(mFonts.get(0), offset, limit));
        }
        return result;
    }

    // base fonts
    @Nonnull
    public List<Font> getFonts() {
        return mFonts;
    }

    // no scores
    private Font getFamilyForChar(int ch) {
        for (Font font : mFonts) {
            if (font.canDisplay(ch)) {
                return font;
            }
        }
        for (Font font : sAllFontFamilies) {
            if (font.canDisplay(ch)) {
                return font;
            }
        }
        return mFonts.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Typeface typeface = (Typeface) o;

        return mFonts.equals(typeface.mFonts);
    }

    @Override
    public int hashCode() {
        return mFonts.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Typeface{");
        for (int i = 0, e = mFonts.size(); i < e; i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(mFonts.get(i).getFamily(Locale.ROOT));
        }
        return b.append('}').toString();
    }
}
