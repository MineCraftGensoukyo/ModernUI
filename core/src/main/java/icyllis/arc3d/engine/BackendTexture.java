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

package icyllis.arc3d.engine;

import icyllis.modernui.annotation.NonNull;
import icyllis.arc3d.opengl.GLServer;
import icyllis.arc3d.opengl.GLTextureInfo;
import icyllis.arc3d.vulkan.VulkanImageInfo;

/**
 * A BackendTexture instance is initialized once, and may be shared.
 */
public abstract class BackendTexture {

    protected final int mWidth;
    protected final int mHeight;

    protected BackendTexture(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * @return see Types
     */
    public abstract int getBackend();

    /**
     * @return width in pixels
     */
    public final int getWidth() {
        return mWidth;
    }

    /**
     * @return height in pixels
     */
    public final int getHeight() {
        return mHeight;
    }

    /**
     * @return external texture
     */
    public abstract boolean isExternal();

    /**
     * @return whether the texture has mip levels allocated or not
     */
    public abstract boolean isMipmapped();

    /**
     * If the backend API is OpenGL, copies a snapshot of the GLTextureInfo struct into the passed
     * in pointer and returns true. Otherwise, returns false if the backend API is not OpenGL.
     */
    public boolean getGLTextureInfo(GLTextureInfo info) {
        return false;
    }

    /**
     * Call this to indicate that the texture parameters have been modified in the GL context
     * externally to Context.
     * <p>
     * Tells client that these parameters of the texture are changed out of client control
     * (for example, you called glTexParameteri without using {@link GLServer}).
     * The local states will be forced to reset to a known state when next use.
     */
    public void glTextureParametersModified() {
    }

    /**
     * If the backend API is Vulkan, copies a snapshot of the VkImageInfo struct into the passed
     * in pointer and returns true. This snapshot will set the mImageLayout to the current layout
     * state. Otherwise, returns false if the backend API is not Vulkan.
     */
    public boolean getVkImageInfo(VulkanImageInfo info) {
        return false;
    }

    /**
     * Anytime the client changes the VkImageLayout of the VkImage captured by this
     * BackendTexture, they must call this function to notify pipeline of the changed layout.
     */
    public void setVkImageLayout(int layout) {
    }

    /**
     * Anytime the client changes the QueueFamilyIndex of the VkImage captured by this
     * BackendTexture, they must call this function to notify pipeline of the changed layout.
     */
    public void setVkQueueFamilyIndex(int queueFamilyIndex) {
    }

    /**
     * Get the BackendFormat for this texture.
     */
    @NonNull
    public abstract BackendFormat getBackendFormat();

    /**
     * Returns true if we are working with protected content.
     */
    public abstract boolean isProtected();

    /**
     * Returns true if both textures are valid and refer to the same API texture.
     */
    public abstract boolean isSameTexture(BackendTexture texture);
}
