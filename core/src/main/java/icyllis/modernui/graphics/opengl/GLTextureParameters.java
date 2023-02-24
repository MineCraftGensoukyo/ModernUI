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

package icyllis.modernui.graphics.opengl;

public final class GLTextureParameters {

    // Texture parameter state that is not overridden by a bound sampler object.
    public int mBaseMipMapLevel;
    public int mMaxMipmapLevel;
    public boolean mSwizzleIsRGBA;

    public GLTextureParameters() {
        // These are the OpenGL defaults.
        mBaseMipMapLevel = 0;
        mMaxMipmapLevel = 1000;
        mSwizzleIsRGBA = true;
    }

    /**
     * Makes parameters invalid, forces GLServer to refresh.
     */
    public void invalidate() {
        mBaseMipMapLevel = ~0;
        mMaxMipmapLevel = ~0;
        mSwizzleIsRGBA = false;
    }

    @Override
    public String toString() {
        return "{" +
                "mBaseMipMapLevel=" + mBaseMipMapLevel +
                ", mMaxMipmapLevel=" + mMaxMipmapLevel +
                ", mSwizzleIsRGBA=" + mSwizzleIsRGBA +
                '}';
    }
}
