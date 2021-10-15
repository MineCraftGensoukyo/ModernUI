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

package icyllis.modernui;

import icyllis.modernui.text.Typeface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

/**
 * The core class of the client side of Modern UI
 */
public class ModernUI {

    public static final String ID = "modernui"; // as well as the namespace
    public static final String NAME_CPT = "ModernUI";

    public static final Logger LOGGER = LogManager.getLogger(NAME_CPT);
    public static final Marker MARKER = MarkerManager.getMarker("Core");

    protected static volatile ModernUI sInstance;

    private static final Cleaner sCleaner = Cleaner.create();

    private final Path mAssetsDir = Path.of(String.valueOf((System.getenv("APP_ASSETS"))));

    public ModernUI() {
        synchronized (ModernUI.class) {
            if (sInstance == null) {
                sInstance = this;
            }
        }
        if (Runtime.version().feature() < 16) {
            throw new RuntimeException("JRE 16 or above is required");
        }
    }

    /**
     * Gets Modern UI instance.
     *
     * @return the Modern UI
     */
    public static ModernUI get() {
        return sInstance;
    }

    /**
     * Registers a target and a cleaning action to run when the target becomes phantom
     * reachable. It will be registered with the global cleaner shared across Modern UI.
     * The action object should never hold any reference of the target object.
     *
     * @param target the target to monitor
     * @param action a {@code Runnable} to invoke when the target becomes phantom reachable
     * @return a {@code Cleanable} instance for explicit cleaning
     */
    @Nonnull
    public static Cleanable registerCleanup(@Nonnull Object target, @Nonnull Runnable action) {
        return sCleaner.register(target, action);
    }

    @Nonnull
    public Locale getSelectedLocale() {
        return Locale.getDefault();
    }

    @Nonnull
    public Typeface getSelectedTypeface() {
        return Typeface.INTERNAL;
    }

    /**
     * Whether to enable RTL support, it should always be true.
     *
     * @return whether RTL is supported
     */
    public boolean hasRtlSupport() {
        return true;
    }

    @Nonnull
    public InputStream getResourceAsStream(@Nonnull String namespace, @Nonnull String path) throws IOException {
        return new FileInputStream(mAssetsDir.resolve(namespace).resolve(path).toFile());
    }

    @Nonnull
    public ReadableByteChannel getResourceAsChannel(@Nonnull String namespace, @Nonnull String path) throws IOException {
        return Files.newByteChannel(mAssetsDir.resolve(namespace).resolve(path), StandardOpenOption.READ);
    }
}
