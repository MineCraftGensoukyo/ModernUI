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

import icyllis.arc3d.core.SharedPtr;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static icyllis.arc3d.engine.Engine.BudgetType;

/**
 * Base class for operating backend memory objects that can be kept in the
 * {@link ResourceCache}. Such resources will have a large memory allocation.
 * Possible implementations:
 * <ul>
 *   <li>GLBuffer</li>
 *   <li>GLTexture</li>
 *   <li>GLRenderbuffer</li>
 *   <li>VkBuffer</li>
 *   <li>VkImage</li>
 * </ul>
 * Register resources into the cache to track their GPU memory usage. Since all
 * Java objects will always be strong referenced, an explicit {@link #ref()} and
 * {@link #unref()} is required to determine if they need to be recycled or not.
 * When used as smart pointers, they need to be annotated as {@link SharedPtr},
 * otherwise they tend to be used as raw pointers (no ref/unref calls should be
 * made). Java object's reference, or identity, can be used as unique identifiers.
 * <p>
 * Each {@link Resource} should be created with immutable GPU memory allocation.
 * Use {@link ResourceProvider} to obtain {@link Resource} objects.
 */
@NotThreadSafe
public abstract class Resource {

    private static final VarHandle REF_CNT;
    private static final VarHandle COMMAND_BUFFER_USAGE_CNT;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            REF_CNT = lookup.findVarHandle(Resource.class, "mRefCnt", int.class);
            COMMAND_BUFFER_USAGE_CNT = lookup.findVarHandle(Resource.class, "mCommandBufferUsageCnt", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("FieldMayBeFinal")
    private volatile int mRefCnt = 1;
    @SuppressWarnings("FieldMayBeFinal")
    private volatile int mCommandBufferUsageCnt = 0;

    static final PriorityQueue.Accessor<Resource> QUEUE_ACCESSOR = new PriorityQueue.Accessor<>() {
        @Override
        public void setIndex(Resource resource, int index) {
            resource.mCacheIndex = index;
        }

        @Override
        public int getIndex(Resource resource) {
            return resource.mCacheIndex;
        }
    };

    // the index into a heap when this resource is cleanable or an array when not,
    // this is maintained by the cache
    int mCacheIndex = -1;
    // the value reflects how recently this resource was accessed in the cache,
    // this is maintained by the cache
    int mTimestamp;
    private long mCleanUpTime;

    // null meaning invalid, lazy initialized
    Object mScratchKey;
    Object mUniqueKey;

    // set once in constructor, clear to null after being destroyed
    Server mServer;

    private byte mBudgetType = BudgetType.NotBudgeted;
    private boolean mWrapped = false;

    /**
     * An object with identity.
     */
    public static final class UniqueID {

        @Nonnull
        private String mLabel = "";

        public UniqueID() {
            assert hashCode() == System.identityHashCode(this);
        }

        @Nonnull
        @Override
        public String toString() {
            return "GpuResource.UniqueID" + "@" + Integer.toHexString(hashCode()) + "{" + mLabel + "}";
        }
    }

    private final UniqueID mUniqueID = new UniqueID();

    protected Resource(Server server) {
        assert (server != null);
        mServer = server;
    }

    @SharedPtr
    public static <T extends Resource> T move(@SharedPtr T sp) {
        if (sp != null)
            sp.unref();
        return null;
    }

    @SharedPtr
    public static <T extends Resource> T move(@SharedPtr T sp, @SharedPtr T that) {
        if (sp != null)
            sp.unref();
        return that;
    }

    @SharedPtr
    public static <T extends Resource> T create(@SharedPtr T that) {
        if (that != null)
            that.ref();
        return that;
    }

    @SharedPtr
    public static <T extends Resource> T create(@SharedPtr T sp, @SharedPtr T that) {
        if (sp != null)
            sp.unref();
        if (that != null)
            that.ref();
        return that;
    }

    /**
     * @return true if this resource is uniquely referenced by the client pipeline
     */
    public final boolean unique() {
        // std::memory_order_acquire, maybe volatile?
        return (int) REF_CNT.getAcquire(this) == 1;
    }

    /**
     * Increases the reference count by 1 on the client pipeline.
     * It's an error to call this method if the reference count has already reached zero.
     */
    public final void ref() {
        // only the cache should be able to add the first ref to a resource.
        assert hasRef();
        // stronger than std::memory_order_relaxed
        REF_CNT.getAndAddRelease(this, 1);
    }

    /**
     * Decreases the reference count by 1 on the client pipeline.
     * It's an error to call this method if the reference count has already reached zero.
     */
    public final void unref() {
        assert hasRef();
        // stronger than std::memory_order_acq_rel
        if ((int) REF_CNT.getAndAdd(this, -1) == 1) {
            notifyACntReachedZero(false);
        }
    }

    /**
     * Increases the usage count by 1 on the tracked backend pipeline.
     * <p>
     * This is designed to be used by Resources that need to track when they are in use on
     * backend (usually via a command buffer) separately from tracking if there are any current logical
     * usages in client. This allows for a scratch Resource to be reused for new draw calls even
     * if it is in use on the backend.
     */
    public final void addCommandBufferUsage() {
        // stronger than std::memory_order_relaxed
        COMMAND_BUFFER_USAGE_CNT.getAndAddRelease(this, 1);
    }

    /**
     * Decreases the usage count by 1 on the tracked backend pipeline.
     * It's an error to call this method if the usage count has already reached zero.
     */
    public final void removeCommandBufferUsage() {
        assert hasCommandBufferUsage();
        // stronger than std::memory_order_acq_rel
        if ((int) COMMAND_BUFFER_USAGE_CNT.getAndAdd(this, -1) == 1) {
            notifyACntReachedZero(true);
        }
    }

    protected final boolean hasRef() {
        // std::memory_order_relaxed, maybe acquire?
        return (int) REF_CNT.getOpaque(this) > 0;
    }

    protected final boolean hasCommandBufferUsage() {
        // std::memory_order_acquire barrier is only really needed if we return false
        // it prevents code conditioned on the result of hasCommandBufferUsage() from running
        // until previous owners are all totally done calling removeCommandBufferUsage()
        return (int) COMMAND_BUFFER_USAGE_CNT.getAcquire(this) > 0;
    }

    // Privileged method that allows going from ref count = 0 to ref count = 1
    final void addInitialRef() {
        // assert (int) REF_CNT.getAcquire(this) >= 0;
        // stronger than std::memory_order_relaxed
        REF_CNT.getAndAddRelease(this, 1);
    }

    // Either ref cnt or command buffer usage cnt reached zero
    private void notifyACntReachedZero(boolean commandBufferUsage) {
        if (mServer == null) {
            // If we have no ref and no command buffer usage, then we've already been removed from the cache,
            // and then this Java object should be phantom reachable soon after (GC-ed).
            // Otherwise, either ref and command buffer usage is not 0, then this Java object will still be
            // strongly referenced, but we don't check the consistency here.
            // We assume this Java object will eventually be garbage-collected, no matter what XX cnt is.
            return;
        }

        mServer.getContext().getResourceCache().notifyACntReachedZero(this, commandBufferUsage);
    }

    /**
     * Checks whether an object has been released or discarded. All objects will
     * be in this state after their creating Context is destroyed or has
     * contextLost called. It's up to the client to test isDestroyed() before
     * attempting to use an object if it holds refs on objects across
     * Context.close(), freeResources with the force flag, or contextLost.
     *
     * @return true if the object has been released or discarded, false otherwise.
     */
    public final boolean isDestroyed() {
        return mServer == null;
    }

    /**
     * Retrieves the context that owns the object. Note that it is possible for
     * this to return null. When objects have been release()ed or discard()ed
     * they no longer have an owning context. Destroying a DirectContext
     * automatically releases all its resources.
     */
    @Nullable
    public final DirectContext getContext() {
        return mServer != null ? mServer.getContext() : null;
    }

    /**
     * Retrieves the amount of GPU memory used by this resource in bytes. It is
     * approximate since we aren't aware of additional padding or copies made
     * by the driver.
     * <p>
     * <b>NOTE: The return value must be constant in this object.</b>
     *
     * @return the amount of GPU memory used in bytes
     */
    public abstract long getMemorySize();

    /**
     * Returns the current unique key for the resource. It will be invalid if the resource has no
     * associated unique key.
     */
    @Nullable
    public final Object getUniqueKey() {
        return mUniqueKey;
    }

    /**
     * Gets a tag that is unique for this Resource object. It is static in that it does
     * not change when the content of the Resource object changes. It has identity and
     * never hold a reference to this Resource object, so it can be used to track state
     * changes through '=='.
     */
    @Nonnull
    public UniqueID getUniqueID() {
        return mUniqueID;
    }

    /**
     * @return the label for the resource, or empty
     */
    @Nonnull
    public final String getLabel() {
        return mUniqueID.mLabel;
    }

    /**
     * Sets a label for the resource for debugging purposes.
     *
     * @param label the new label to set, or empty to clear
     */
    public final void setLabel(String label) {
        label = label != null ? label.trim() : "";
        if (!mUniqueID.mLabel.equals(label)) {
            mUniqueID.mLabel = label;
            onSetLabel(label);
        }
    }

    /**
     * Sets a unique key for the resource. If the resource was previously cached as scratch it will
     * be converted to a uniquely-keyed resource. If the key is invalid then this is equivalent to
     * removeUniqueKey(). If another resource is using the key then its unique key is removed and
     * this resource takes over the key.
     */
    @ApiStatus.Internal
    public final void setUniqueKey(Object key) {
        assert hasRef();

        // Uncached resources can never have a unique key, unless they're wrapped resources. Wrapped
        // resources are a special case: the unique keys give us a weak ref so that we can reuse the
        // same resource (rather than re-wrapping). When a wrapped resource is no longer referenced,
        // it will always be released - it is never converted to a scratch resource.
        if (mBudgetType != BudgetType.Budgeted && !mWrapped) {
            return;
        }

        if (mServer == null) {
            return;
        }

        mServer.getContext().getResourceCache().changeUniqueKey(this, key);
    }

    /**
     * Removes the unique key from a resource. If the resource has a scratch key, it may be
     * preserved for recycling as scratch.
     */
    @ApiStatus.Internal
    public final void removeUniqueKey() {
        if (mServer == null) {
            return;
        }

        mServer.getContext().getResourceCache().removeUniqueKey(this);
    }

    /**
     * Budgeted: If the resource is uncached make it cached. Has no effect on resources that
     * are wrapped or already cached.
     * <p>
     * Not Budgeted: If the resource is cached make it uncached. Has no effect on resources that
     * are wrapped or already uncached. Furthermore, resources with unique keys cannot be made
     * not budgeted.
     */
    @ApiStatus.Internal
    public final void makeBudgeted(boolean budgeted) {
        if (budgeted) {
            // We should never make a wrapped resource budgeted.
            assert !mWrapped;
            // Only wrapped resources can be in the cacheable budgeted state.
            assert mBudgetType != BudgetType.WrapCacheable;
            if (mServer != null && mBudgetType == BudgetType.NotBudgeted) {
                // Currently, resources referencing wrapped objects are not budgeted.
                mBudgetType = BudgetType.Budgeted;
                mServer.getContext().getResourceCache().didChangeBudgetStatus(this);
            }
        } else {
            if (mServer != null && mBudgetType == BudgetType.Budgeted && mUniqueKey == null) {
                mBudgetType = BudgetType.NotBudgeted;
                mServer.getContext().getResourceCache().didChangeBudgetStatus(this);
            }
        }
    }

    /**
     * Get the resource's budget type which indicates whether it counts against the resource cache
     * budget and if not whether it is allowed to be cached.
     */
    @ApiStatus.Internal
    public final int getBudgetType() {
        assert mBudgetType == BudgetType.Budgeted || mWrapped || mUniqueKey == null;
        return mBudgetType;
    }

    /**
     * Is the resource object wrapping an externally allocated GPU resource?
     */
    @ApiStatus.Internal
    public final boolean isWrapped() {
        return mWrapped;
    }

    /**
     * If this resource can be used as a scratch resource this returns a valid scratch key.
     * Otherwise, it returns a key for which isNullScratch is true. The resource may currently be
     * used as a uniquely keyed resource rather than scratch. Check isScratch().
     */
    @ApiStatus.Internal
    @Nullable
    public final Object getScratchKey() {
        return mScratchKey;
    }

    /**
     * If the resource has a scratch key, the key will be removed. Since scratch keys are installed
     * at resource creation time, this means the resource will never again be used as scratch.
     */
    @ApiStatus.Internal
    public final void removeScratchKey() {
        if (mServer != null && mScratchKey != null) {
            mServer.getContext().getResourceCache().willRemoveScratchKey(this);
            mScratchKey = null;
        }
    }

    @ApiStatus.Internal
    public final boolean isCleanable() {
        // Resources in the cacheable budgeted state are never cleanable when they have a unique
        // key. The key must be removed/invalidated to make them cleanable.
        return !hasRef() && !hasCommandBufferUsage() &&
                !(mBudgetType == BudgetType.WrapCacheable && mUniqueKey != null);
    }

    @ApiStatus.Internal
    public final boolean hasRefOrCommandBufferUsage() {
        return hasRef() || hasCommandBufferUsage();
    }

    /**
     * This must be called by every non-wrapped subclass. It should be called once the object is
     * fully initialized (i.e. only from the constructors of the final class).
     *
     * @param budgeted budgeted or not
     */
    protected final void registerWithCache(boolean budgeted) {
        assert mBudgetType == BudgetType.NotBudgeted;
        mBudgetType = budgeted ? BudgetType.Budgeted : BudgetType.NotBudgeted;
        mScratchKey = computeScratchKey();
        mServer.getContext().getResourceCache().insertResource(this);
    }

    /**
     * This must be called by every subclass that references any wrapped backend objects. It
     * should be called once the object is fully initialized (i.e. only from the constructors of the
     * final class).
     *
     * @param cacheable cacheable or not
     */
    protected final void registerWithCacheWrapped(boolean cacheable) {
        assert mBudgetType == BudgetType.NotBudgeted;
        // Resources referencing wrapped objects are never budgeted. They may be cached or uncached.
        mBudgetType = cacheable ? BudgetType.WrapCacheable : BudgetType.NotBudgeted;
        mWrapped = true;
        mServer.getContext().getResourceCache().insertResource(this);
    }

    /**
     * @return the server or null if destroyed
     */
    protected Server getServer() {
        return mServer;
    }

    /**
     * Subclass should override this method to free GPU resources in the backend API.
     */
    protected abstract void onRelease();

    /**
     * Subclass should override this method to invalidate any internal handles, etc.
     * to backend API resources. This may be called when the underlying 3D context
     * is no longer valid and so no backend API calls should be made.
     */
    protected abstract void onDiscard();

    protected void onSetLabel(@Nonnull String label) {
    }

    /**
     * Called by the registerWithCache if the resource is available to be used as scratch.
     * Resource subclasses should override this if the instances should be recycled as scratch
     * resources and populate the scratchKey with the key.
     * By default, resources are not recycled as scratch.
     */
    @Nullable
    protected Object computeScratchKey() {
        return null;
    }

    /**
     * Is the resource currently cached as scratch? This means it is cached, has a valid scratch
     * key, and does not have a unique key.
     */
    final boolean isScratch() {
        return mBudgetType == BudgetType.Budgeted && mScratchKey != null && mUniqueKey == null;
    }

    final boolean isUsableAsScratch() {
        return isScratch() && !hasRef();
    }

    /**
     * Called by the cache to delete the resource under normal circumstances.
     */
    final void release() {
        assert mServer != null;
        onRelease();
        mServer.getContext().getResourceCache().removeResource(this);
        mServer = null;
    }

    /**
     * Called by the cache to delete the resource when the backend 3D context is no longer valid.
     */
    final void discard() {
        if (mServer == null) {
            return;
        }
        onDiscard();
        mServer.getContext().getResourceCache().removeResource(this);
        mServer = null;
    }

    final void setCleanUpTime() {
        assert isCleanable();
        mCleanUpTime = System.nanoTime();
    }

    /**
     * Called by the cache to determine whether this resource should be cleaned up based on the length
     * of time it has been available for cleaning.
     */
    final long getCleanUpTime() {
        assert isCleanable();
        return mCleanUpTime;
    }
}
