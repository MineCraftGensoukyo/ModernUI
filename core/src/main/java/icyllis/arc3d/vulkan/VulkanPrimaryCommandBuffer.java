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

package icyllis.arc3d.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkDevice;

import static icyllis.arc3d.vulkan.VKCore.*;

public final class VulkanPrimaryCommandBuffer extends VulkanCommandBuffer {

    private VulkanPrimaryCommandBuffer(VkDevice device, long handle) {
        super(device, handle);
    }

    public static VulkanPrimaryCommandBuffer create(VulkanServer server,
                                                    long commandPool) {
        try (var stack = MemoryStack.stackPush()) {
            var pCommandBuffer = stack.mallocPointer(1);
            var result = vkAllocateCommandBuffers(
                    server.device(),
                    VkCommandBufferAllocateInfo
                            .malloc(stack)
                            .sType$Default()
                            .pNext(0)
                            .commandPool(commandPool)
                            .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                            .commandBufferCount(1),
                    pCommandBuffer
            );
            if (result != VK_SUCCESS) {
                return null;
            }
            return new VulkanPrimaryCommandBuffer(server.device(), pCommandBuffer.get(0));
        }
    }
}
