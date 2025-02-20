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

import icyllis.arc3d.core.Rect2i;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Set;

/**
 * Tracks the state across all the GrOps (really just the GrDrawOps) in a OpsTask flush.
 */
public class OpFlushState implements MeshDrawTarget {

    private final Server mServer;

    private OpsRenderPass mOpsRenderPass;

    public OpFlushState(Server server,
                        ResourceProvider resourceProvider) {
        mServer = server;
    }

    public Server getServer() {
        return mServer;
    }

    public final PipelineState findOrCreatePipelineState(final PipelineInfo pipelineInfo) {
        return mServer.getContext().findOrCreatePipelineState(pipelineInfo);
    }

    @Override
    public long makeVertexSpace(Mesh mesh) {
        return mServer.getVertexPool().makeSpace(mesh);
    }

    @Override
    public long makeInstanceSpace(Mesh mesh) {
        return mServer.getInstancePool().makeSpace(mesh);
    }

    @Override
    public long makeIndexSpace(Mesh mesh) {
        return mServer.getIndexPool().makeSpace(mesh);
    }

    @Nullable
    @Override
    public ByteBuffer makeVertexWriter(Mesh mesh) {
        return mServer.getVertexPool().makeWriter(mesh);
    }

    @Nullable
    @Override
    public ByteBuffer makeInstanceWriter(Mesh mesh) {
        return mServer.getInstancePool().makeWriter(mesh);
    }

    @Nullable
    @Override
    public ByteBuffer makeIndexWriter(Mesh mesh) {
        return mServer.getIndexPool().makeWriter(mesh);
    }

    public OpsRenderPass getOpsRenderPass() {
        return mOpsRenderPass;
    }

    public OpsRenderPass beginOpsRenderPass(SurfaceProxyView writeView,
                                            Rect2i contentBounds,
                                            byte colorOps,
                                            byte stencilOps,
                                            float[] clearColor,
                                            Set<TextureProxy> sampledTextures,
                                            int pipelineFlags) {
        assert (mOpsRenderPass == null);
        OpsRenderPass opsRenderPass = mServer.getOpsRenderPass(writeView, contentBounds,
                colorOps, stencilOps, clearColor,
                sampledTextures, pipelineFlags);
        if (opsRenderPass == null) {
            return null;
        }
        mOpsRenderPass = opsRenderPass;
        opsRenderPass.begin();
        return opsRenderPass;
    }

    public void reset() {
    }
}
