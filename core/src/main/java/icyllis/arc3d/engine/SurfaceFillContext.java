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
import icyllis.arc3d.engine.ops.OpsTask;
import icyllis.modernui.graphics.RefCnt;

public class SurfaceFillContext extends SurfaceContext {

    @SharedPtr
    private OpsTask mOpsTask;

    private final SurfaceProxyView mWriteView;

    public SurfaceFillContext(RecordingContext context,
                              SurfaceProxyView readView,
                              SurfaceProxyView writeView,
                              int colorInfo) {
        super(context, readView, colorInfo);
        mWriteView = writeView;
    }

    public OpsTask getOpsTask() {
        assert mContext.isOwnerThread();
        if (mOpsTask == null || mOpsTask.isClosed()) {
            return nextOpsTask();
        }
        return mOpsTask;
    }

    public OpsTask nextOpsTask() {
        OpsTask newOpsTask = getDrawingManager().newOpsTask(mWriteView);
        mOpsTask = RefCnt.move(mOpsTask, newOpsTask);
        return mOpsTask;
    }
}
