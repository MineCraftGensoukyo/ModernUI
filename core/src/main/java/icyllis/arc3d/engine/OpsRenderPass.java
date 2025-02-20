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

import icyllis.arc3d.core.Rect2f;
import icyllis.arc3d.engine.ops.Op;

/**
 * The {@link OpsRenderPass} is a series of commands (draws, clears, and discards), which all target the
 * same render target. {@link Op Ops} execute into a {@link OpsRenderPass}.
 */
//TODO
public abstract class OpsRenderPass {

    /**
     * DrawPipelineStatus.
     */
    private static final int
            kConfigured_DrawPipelineStatus = 0,
            kNotConfigured_DrawPipelineStatus = 1,
            kFailedToBind_DrawPipelineStatus = 2;

    private int mDrawPipelineStatus = kNotConfigured_DrawPipelineStatus;

    protected RenderTarget mRenderTarget;
    protected int mSurfaceOrigin;

    private TextureProxy[] mGeomTextures = new TextureProxy[1];

    public OpsRenderPass() {
        this(null, Engine.SurfaceOrigin.kUpperLeft);
    }

    public OpsRenderPass(RenderTarget fs, int origin) {
        mRenderTarget = fs;
        mSurfaceOrigin = origin;
    }

    public void begin() {
        mDrawPipelineStatus = kNotConfigured_DrawPipelineStatus;
    }

    public void end() {
    }

    /**
     * Clear the owned render target. Clears the full target if 'scissor' is disabled, otherwise it
     * is restricted to 'scissor'.
     */
    public void clearColor(int left, int top, int right, int bottom,
                           float red, float green, float blue, float alpha) {
        assert (mRenderTarget != null);
        mDrawPipelineStatus = kNotConfigured_DrawPipelineStatus;
    }

    /**
     * Same as {@link #clearColor} but modifies the stencil.
     */
    public void clearStencil(int left, int top, int right, int bottom, boolean insideMask) {
        assert (mRenderTarget != null);
        mDrawPipelineStatus = kNotConfigured_DrawPipelineStatus;
    }

    /**
     * Updates the internal pipeline state for drawing. Enters an internal "bad" state if
     * the pipeline could not be set.
     *
     * @param pipelineInfo  the pipeline info used to update uniforms
     * @param pipelineState the pipeline state object
     * @param drawBounds    the sub-area of render target for subsequent draw calls
     */
    public void bindPipeline(PipelineInfo pipelineInfo, PipelineState pipelineState, Rect2f drawBounds) {
        assert (pipelineInfo.origin() == mSurfaceOrigin);
        if (!onBindPipeline(pipelineInfo, pipelineState, drawBounds)) {
            mDrawPipelineStatus = kFailedToBind_DrawPipelineStatus;
            return;
        }

        mDrawPipelineStatus = kConfigured_DrawPipelineStatus;
    }

    /**
     * Single texture version of {@link #bindTextures(TextureProxy[])}.
     *
     * @param geomTexture the raw ptr to textures at binding 0
     */
    public final void bindTexture(TextureProxy geomTexture) {
        mGeomTextures[0] = geomTexture;
        bindTextures(mGeomTextures);
        mGeomTextures[0] = null;
    }

    /**
     * Binds textures for the geometry processor. Texture bindings are dynamic state and therefore
     * not set during {@link #bindPipeline}. If the current program uses textures,
     * then the client must call this method before drawing. The geometry processor textures may also
     * be updated between draws by calling this method again with a different array for textures.
     * <p>
     * Note that this method is only used for GP using texture. If GP does not use texture but FP does,
     * they will be automatically set during {@link #bindPipeline}, and this is
     * a no-op. Otherwise, this method must be called if the GP uses textures.
     *
     * @param geomTextures the raw ptr to textures starting from binding 0
     */
    public final void bindTextures(TextureProxy[] geomTextures) {
        //TODO
    }

    /**
     * Binds geometric (input) buffers to current command buffer.
     *
     * @param indexBuffer    raw ptr to the index buffer if using indexed rendering, or nullptr
     * @param vertexBuffer   raw ptr to the vertex buffer, can be nullptr
     * @param instanceBuffer raw ptr to the instance buffer if using instanced rendering, or nullptr
     */
    public final void bindBuffers(Buffer indexBuffer,
                                  Buffer vertexBuffer,
                                  Buffer instanceBuffer) {
        if (vertexBuffer == null && instanceBuffer == null) {
            mDrawPipelineStatus = kFailedToBind_DrawPipelineStatus;
            return;
        }
        if (mDrawPipelineStatus == kConfigured_DrawPipelineStatus) {
            onBindBuffers(indexBuffer, vertexBuffer, instanceBuffer);
        } else {
            assert (mDrawPipelineStatus == kFailedToBind_DrawPipelineStatus);
        }
    }

    /**
     * Records a non-indexed draw to current command buffer.
     *
     * @param vertexCount the number of vertices to draw
     * @param baseVertex  the index of the first vertex to draw
     */
    public final void draw(int vertexCount, int baseVertex) {
        if (mDrawPipelineStatus == kConfigured_DrawPipelineStatus) {
            onDraw(vertexCount, baseVertex);
            getServer().getStats().incNumDraws();
        } else {
            assert (mDrawPipelineStatus == kFailedToBind_DrawPipelineStatus);
            getServer().getStats().incNumFailedDraws();
        }
    }

    /**
     * Records an indexed draw to current command buffer.
     *
     * @param indexCount the number of vertices to draw
     * @param baseIndex  the base index within the index buffer
     * @param baseVertex the value added to the vertex index before indexing into the vertex buffer
     */
    public final void drawIndexed(int indexCount, int baseIndex,
                                  int baseVertex) {
        if (mDrawPipelineStatus == kConfigured_DrawPipelineStatus) {
            onDrawIndexed(indexCount, baseIndex, baseVertex);
            getServer().getStats().incNumDraws();
        } else {
            assert (mDrawPipelineStatus == kFailedToBind_DrawPipelineStatus);
            getServer().getStats().incNumFailedDraws();
        }
    }

    /**
     * Records a non-indexed draw to current command buffer.
     *
     * @param instanceCount the number of instances to draw
     * @param baseInstance  the instance ID of the first instance to draw
     * @param vertexCount   the number of vertices to draw
     * @param baseVertex    the index of the first vertex to draw
     */
    public final void drawInstanced(int instanceCount, int baseInstance,
                                    int vertexCount, int baseVertex) {
        if (mDrawPipelineStatus == kConfigured_DrawPipelineStatus) {
            onDrawInstanced(instanceCount, baseInstance, vertexCount, baseVertex);
            getServer().getStats().incNumDraws();
        } else {
            assert (mDrawPipelineStatus == kFailedToBind_DrawPipelineStatus);
            getServer().getStats().incNumFailedDraws();
        }
    }

    /**
     * Records an indexed draw to current command buffer.
     *
     * @param indexCount    the number of vertices to draw
     * @param baseIndex     the base index within the index buffer
     * @param instanceCount the number of instances to draw
     * @param baseInstance  the instance ID of the first instance to draw
     * @param baseVertex    the value added to the vertex index before indexing into the vertex buffer
     */
    public final void drawIndexedInstanced(int indexCount, int baseIndex,
                                           int instanceCount, int baseInstance,
                                           int baseVertex) {
        if (mDrawPipelineStatus == kConfigured_DrawPipelineStatus) {
            onDrawIndexedInstanced(indexCount, baseIndex, instanceCount, baseInstance, baseVertex);
            getServer().getStats().incNumDraws();
        } else {
            assert (mDrawPipelineStatus == kFailedToBind_DrawPipelineStatus);
            getServer().getStats().incNumFailedDraws();
        }
    }

    protected void set(RenderTarget fs, int origin) {
        assert (mRenderTarget == null);
        mRenderTarget = fs;
        mSurfaceOrigin = origin;
    }

    protected abstract Server getServer();

    protected abstract boolean onBindPipeline(PipelineInfo pipelineInfo,
                                              PipelineState pipelineState,
                                              Rect2f drawBounds);

    protected abstract void onBindBuffers(Buffer indexBuffer,
                                          Buffer vertexBuffer,
                                          Buffer instanceBuffer);

    protected abstract void onDraw(int vertexCount, int baseVertex);

    protected abstract void onDrawIndexed(int indexCount, int baseIndex,
                                          int baseVertex);

    protected abstract void onDrawInstanced(int instanceCount, int baseInstance,
                                            int vertexCount, int baseVertex);

    protected abstract void onDrawIndexedInstanced(int indexCount, int baseIndex,
                                                   int instanceCount, int baseInstance,
                                                   int baseVertex);
}
