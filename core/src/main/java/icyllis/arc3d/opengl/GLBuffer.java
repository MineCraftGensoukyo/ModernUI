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

import icyllis.arc3d.core.SharedPtr;
import icyllis.arc3d.engine.*;
import icyllis.modernui.graphics.RefCnt;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static icyllis.arc3d.opengl.GLCore.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class GLBuffer extends Buffer {

    private int mBuffer;

    private boolean mLocked;

    private long mMappedBuffer;
    @SharedPtr
    private CpuBuffer mStagingBuffer;

    private final boolean mPersistentlyMapped;

    private GLBuffer(GLServer server,
                     int size,
                     int usage,
                     int buffer,
                     long mappedBuffer) {
        super(server, size, usage);
        mBuffer = buffer;
        mMappedBuffer = mappedBuffer;
        mPersistentlyMapped = mMappedBuffer != NULL;

        registerWithCache(true);
    }

    @Nullable
    @SharedPtr
    public static GLBuffer make(GLServer server,
                                int size,
                                int usage) {
        assert (size > 0);

        int typeFlags = usage & (Engine.BufferUsageFlags.kVertex |
                Engine.BufferUsageFlags.kIndex |
                Engine.BufferUsageFlags.kTransferSrc |
                Engine.BufferUsageFlags.kTransferDst |
                Engine.BufferUsageFlags.kUniform |
                Engine.BufferUsageFlags.kDrawIndirect);
        assert typeFlags != 0;

        boolean preferBufferStorage = (usage & Engine.BufferUsageFlags.kStreaming) != 0;

        if (!preferBufferStorage) {
            if (Integer.bitCount(typeFlags) != 1) {
                new Throwable("RHICreateBuffer, only one type bit is allowed, given 0x" +
                        Integer.toHexString(typeFlags))
                        .printStackTrace(server.getContext().getErrorWriter());
                return null;
            }
        }

        if (server.getCaps().hasDSASupport()) {
            int buffer = glCreateBuffers();
            if (buffer == 0) {
                return null;
            }

            long persistentlyMappedBuffer = NULL;

            if (preferBufferStorage) {
                int allocFlags = getBufferStorageFlags(usage);

                if (server.getCaps().skipErrorChecks()) {
                    glNamedBufferStorage(buffer, size, allocFlags);
                } else {
                    glClearErrors();
                    glNamedBufferStorage(buffer, size, allocFlags);
                    if (glGetError() != GL_NO_ERROR) {
                        glDeleteBuffers(buffer);
                        new Throwable("RHICreateBuffer, failed to allocate " + size + " bytes from device")
                                .printStackTrace(server.getContext().getErrorWriter());
                        return null;
                    }
                }
                if ((usage & Engine.BufferUsageFlags.kStreaming) != 0) {
                    persistentlyMappedBuffer = nglMapNamedBufferRange(buffer, 0, size,
                            GL_MAP_WRITE_BIT |
                                    GL_MAP_PERSISTENT_BIT |
                                    GL_MAP_COHERENT_BIT);
                    if (persistentlyMappedBuffer == NULL) {
                        glDeleteBuffers(buffer);
                        new Throwable("RHICreateBuffer, failed to map buffer range persistently")
                                .printStackTrace(server.getContext().getErrorWriter());
                        return null;
                    }
                }
            } else {
                // OpenGL 3.3 uses mutable allocation
                int allocUsage = getBufferUsageM(usage);

                if (server.getCaps().skipErrorChecks()) {
                    glNamedBufferData(buffer, size, allocUsage);
                } else {
                    glClearErrors();
                    glNamedBufferData(buffer, size, allocUsage);
                    if (glGetError() != GL_NO_ERROR) {
                        glDeleteBuffers(buffer);
                        new Throwable("RHICreateBuffer, failed to allocate " + size + " bytes from device")
                                .printStackTrace(server.getContext().getErrorWriter());
                        return null;
                    }
                }
            }

            return new GLBuffer(server, size, usage, buffer, persistentlyMappedBuffer);
        } else {
            int buffer = glGenBuffers();
            if (buffer == 0) {
                return null;
            }

            int target = server.bindBufferForSetup(usage, buffer);
            long persistentlyMappedBuffer = NULL;

            if (preferBufferStorage && server.getCaps().hasBufferStorageSupport()) {
                int allocFlags = getBufferStorageFlags(usage);

                if (server.getCaps().skipErrorChecks()) {
                    glBufferStorage(target, size, allocFlags);
                } else {
                    glClearErrors();
                    glBufferStorage(target, size, allocFlags);
                    if (glGetError() != GL_NO_ERROR) {
                        glDeleteBuffers(buffer);
                        new Throwable("RHICreateBuffer, failed to allocate " + size + " bytes from device")
                                .printStackTrace(server.getContext().getErrorWriter());
                        return null;
                    }
                }
                if ((usage & Engine.BufferUsageFlags.kStreaming) != 0) {
                    persistentlyMappedBuffer = nglMapBufferRange(target, 0, size,
                            GL_MAP_WRITE_BIT |
                                    GL_MAP_PERSISTENT_BIT |
                                    GL_MAP_COHERENT_BIT);
                    if (persistentlyMappedBuffer == NULL) {
                        glDeleteBuffers(buffer);
                        new Throwable("RHICreateBuffer, failed to map buffer range persistently")
                                .printStackTrace(server.getContext().getErrorWriter());
                        return null;
                    }
                }
            } else {
                // OpenGL 3.3 uses mutable allocation
                int allocUsage = getBufferUsageM(usage);

                if (server.getCaps().skipErrorChecks()) {
                    glBufferData(target, size, allocUsage);
                } else {
                    glClearErrors();
                    glBufferData(target, size, allocUsage);
                    if (glGetError() != GL_NO_ERROR) {
                        glDeleteBuffers(buffer);
                        new Throwable("RHICreateBuffer, failed to allocate " + size + " bytes from device")
                                .printStackTrace(server.getContext().getErrorWriter());
                        return null;
                    }
                }
            }

            return new GLBuffer(server, size, usage, buffer, persistentlyMappedBuffer);
        }
    }

    public static int getBufferUsageM(int usage) {
        int allocUsage;
        if ((usage & Engine.BufferUsageFlags.kTransferDst) != 0) {
            allocUsage = GL_DYNAMIC_READ;
        } else if ((usage & Engine.BufferUsageFlags.kStreaming) != 0) {
            allocUsage = GL_STREAM_DRAW;
        } else if ((usage & Engine.BufferUsageFlags.kDynamic) != 0) {
            allocUsage = GL_DYNAMIC_DRAW;
        } else if ((usage & Engine.BufferUsageFlags.kStatic) != 0) {
            allocUsage = GL_STATIC_DRAW;
        } else {
            allocUsage = GL_DYNAMIC_DRAW;
        }
        return allocUsage;
    }

    public static int getBufferStorageFlags(int usage) {
        int allocFlags = 0;
        if ((usage & Engine.BufferUsageFlags.kTransferSrc) != 0) {
            allocFlags |= GL_MAP_WRITE_BIT;
        }
        if ((usage & Engine.BufferUsageFlags.kTransferDst) != 0) {
            allocFlags |= GL_MAP_READ_BIT;
        }
        if ((usage & Engine.BufferUsageFlags.kStreaming) != 0) {
            // no staging buffer, use pinned memory
            allocFlags |= GL_MAP_WRITE_BIT |
                    GL_MAP_PERSISTENT_BIT |
                    GL_MAP_COHERENT_BIT;
        } else if ((usage & (Engine.BufferUsageFlags.kVertex |
                Engine.BufferUsageFlags.kIndex)) != 0) {
            allocFlags |= GL_DYNAMIC_STORAGE_BIT;
        }
        return allocFlags;
    }

    public int getHandle() {
        return mBuffer;
    }

    @Override
    protected void onSetLabel(@Nonnull String label) {
        if (getServer().getCaps().hasDebugSupport()) {
            if (label.isEmpty()) {
                nglObjectLabel(GL_BUFFER, mBuffer, 0, MemoryUtil.NULL);
            } else {
                label = label.substring(0, Math.min(label.length(),
                        getServer().getCaps().maxLabelLength()));
                glObjectLabel(GL_BUFFER, mBuffer, label);
            }
        }
    }

    @Override
    protected void onRelease() {
        if (mBuffer != 0) {
            glDeleteBuffers(mBuffer);
        }
        onDiscard();
    }

    @Override
    protected void onDiscard() {
        mBuffer = 0;
        mLocked = false;
        mMappedBuffer = NULL;
        mStagingBuffer = RefCnt.move(mStagingBuffer);
    }

    @Override
    protected GLServer getServer() {
        return (GLServer) super.getServer();
    }

    @Override
    protected long onLock(int mode, int offset, int size) {
        assert (getServer().getContext().isOwnerThread());
        assert (!mLocked);
        assert (mBuffer != 0);

        mLocked = true;

        if (mPersistentlyMapped) {
            assert (mMappedBuffer != NULL);
            return mMappedBuffer;
        }

        if (mode == kRead_LockMode) {
            // prefer mapping, such as pixel buffer object
            mMappedBuffer = nglMapNamedBufferRange(mBuffer, offset, size, GL_MAP_READ_BIT);
            if (mMappedBuffer == NULL) {
                new Throwable("Failed to map buffer " + this)
                        .printStackTrace(getServer().getContext().getErrorWriter());
            }
            return mMappedBuffer;
        } else {
            // prefer CPU staging buffer
            assert (mode == kWriteDiscard_LockMode);
            mStagingBuffer = getServer().getCpuBufferCache().makeBuffer(size);
            assert (mStagingBuffer != null);
            return mStagingBuffer.data();
        }
    }

    @Override
    protected void onUnlock(int mode, int offset, int size) {
        assert (getServer().getContext().isOwnerThread());
        assert (mLocked);
        assert (mBuffer != 0);

        if (mPersistentlyMapped) {
            assert (mMappedBuffer != NULL);
            // COHERENT == true
        } else if (mode == kRead_LockMode) {
            assert (mMappedBuffer != NULL);
            glUnmapNamedBuffer(mBuffer);
            mMappedBuffer = NULL;
        } else {
            assert (mode == kWriteDiscard_LockMode);
            int target = 0;
            if (!getServer().getCaps().hasDSASupport()) {
                target = getServer().bindBuffer(this);
            }
            if ((mUsage & Engine.BufferUsageFlags.kStatic) == 0) {
                // non-static needs triple buffering, though most GPU drivers did it internally
                doInvalidateBuffer(target, offset, size);
            }
            assert (mStagingBuffer != null);
            doUploadData(target, mStagingBuffer.data(), offset, size);
            mStagingBuffer = RefCnt.move(mStagingBuffer);
        }
        mLocked = false;
    }

    @Override
    public boolean isLocked() {
        return mLocked;
    }

    @Override
    public long getLockedBuffer() {
        if (mLocked) {
            return mMappedBuffer != NULL ? mMappedBuffer : mStagingBuffer.data();
        }
        return NULL;
    }

    @Override
    protected boolean onUpdateData(int offset, int size, long data) {
        assert (getServer().getContext().isOwnerThread());
        assert (mBuffer != 0);
        int target = 0;
        if (!getServer().getCaps().hasDSASupport()) {
            target = getServer().bindBuffer(this);
        }
        if ((mUsage & Engine.BufferUsageFlags.kStatic) == 0) {
            // non-static needs triple buffering, though most GPU drivers did it internally
            if (!doInvalidateBuffer(target, offset, size)) {
                return false;
            }
        }
        doUploadData(target, data, offset, size);
        return true;
    }

    // restricted to 256KB per update
    private static final int MAX_BYTES_PER_UPDATE = 1 << 18;

    private void doUploadData(int target, long data, int offset, int totalSize) {
        if (target == 0) {
            while (totalSize > 0) {
                int size = Math.min(MAX_BYTES_PER_UPDATE, totalSize);
                nglNamedBufferSubData(mBuffer, offset, size, data);
                data += size;
                offset += size;
                totalSize -= size;
            }
        } else {
            while (totalSize > 0) {
                int size = Math.min(MAX_BYTES_PER_UPDATE, totalSize);
                nglBufferSubData(target, offset, size, data);
                data += size;
                offset += size;
                totalSize -= size;
            }
        }
    }

    private boolean doInvalidateBuffer(int target, int offset, int size) {
        // to be honest, invalidation doesn't help performance in most cases
        // because GPU drivers did optimizations
        var server = getServer();
        switch (server.getCaps().getInvalidateBufferType()) {
            case GLCaps.INVALIDATE_BUFFER_TYPE_NULL_DATA -> {
                assert target != 0; // DSA is not support
                int allocUsage = getBufferUsageM(getUsage());
                // orphan full size
                if (server.getCaps().skipErrorChecks()) {
                    glBufferData(target, mSize, allocUsage);
                } else {
                    glClearErrors();
                    glBufferData(target, mSize, allocUsage);
                    if (glGetError() != GL_NO_ERROR) {
                        return false;
                    }
                }
            }
            case GLCaps.INVALIDATE_BUFFER_TYPE_INVALIDATE -> {
                glInvalidateBufferSubData(mBuffer, offset, size);
            }
        }
        return true;
    }
}
