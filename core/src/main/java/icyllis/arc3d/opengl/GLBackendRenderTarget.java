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

package icyllis.arc3d.opengl;

import icyllis.arc3d.engine.BackendRenderTarget;
import icyllis.arc3d.engine.Engine;

import javax.annotation.Nonnull;

public final class GLBackendRenderTarget extends BackendRenderTarget {

    private final int mSampleCount;
    private final int mStencilBits;

    private final GLFramebufferInfo mInfo;

    private GLBackendFormat mBackendFormat;

    // The GLFramebufferInfo can NOT be modified anymore.
    public GLBackendRenderTarget(int width, int height, int sampleCount, int stencilBits,
                                 GLFramebufferInfo info) {
        super(width, height);
        mSampleCount = sampleCount;
        mStencilBits = stencilBits;
        mInfo = info;
        assert sampleCount >= 0;
    }

    @Override
    public int getBackend() {
        return Engine.BackendApi.kOpenGL;
    }

    @Override
    public int getSampleCount() {
        return mSampleCount;
    }

    @Override
    public int getStencilBits() {
        return mStencilBits;
    }

    @Override
    public boolean getGLFramebufferInfo(GLFramebufferInfo info) {
        info.set(mInfo);
        return true;
    }

    @Nonnull
    @Override
    public GLBackendFormat getBackendFormat() {
        if (mBackendFormat == null) {
            mBackendFormat = GLBackendFormat.make(mInfo.mFormat);
        }
        return mBackendFormat;
    }

    @Override
    public boolean isProtected() {
        return mInfo.mIsProtected;
    }
}
