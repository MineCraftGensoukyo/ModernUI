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

import icyllis.arc3d.core.ImageInfo;

public final class DataUtils {

    public static boolean compressionTypeIsOpaque(int compression) {
        return switch (compression) {
            case ImageInfo.COMPRESSION_NONE,
                    ImageInfo.COMPRESSION_BC1_RGB8_UNORM,
                    ImageInfo.COMPRESSION_ETC2_RGB8_UNORM -> true;
            case ImageInfo.COMPRESSION_BC1_RGBA8_UNORM -> false;
            default -> throw new AssertionError(compression);
        };
    }

    public static int num4x4Blocks(int size) {
        return (size + 3) >> 2;
    }

    public static long numBlocks(int compression, int width, int height) {
        return switch (compression) {
            case ImageInfo.COMPRESSION_NONE -> (long) width * height;
            case ImageInfo.COMPRESSION_ETC2_RGB8_UNORM,
                    ImageInfo.COMPRESSION_BC1_RGB8_UNORM,
                    ImageInfo.COMPRESSION_BC1_RGBA8_UNORM -> {
                long numBlocksWidth = num4x4Blocks(width);
                long numBlocksHeight = num4x4Blocks(height);
                yield numBlocksWidth * numBlocksHeight;
            }
            default -> throw new AssertionError(compression);
        };
    }

    private DataUtils() {
    }
}
