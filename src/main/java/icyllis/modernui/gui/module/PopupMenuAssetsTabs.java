/*
 * Modern UI.
 * Copyright (C) 2019 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.gui.module;

import icyllis.modernui.gui.element.IElement;
import icyllis.modernui.gui.master.GlobalModuleManager;
import icyllis.modernui.gui.widget.DropDownList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.ArrayList;
import java.util.List;

public class PopupMenuAssetsTabs implements IGuiModule {

    private List<IElement> elements = new ArrayList<>();

    private List<IGuiEventListener> listeners = new ArrayList<>();

    public PopupMenuAssetsTabs(DropDownList list) {
        elements.add(list);
        listeners.add(list);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        for (IGuiEventListener listener : getEventListeners()) {
            listener.mouseClicked(mouseX, mouseY, mouseButton);
        }
        Minecraft.getInstance().deferTask(GlobalModuleManager.INSTANCE::closePopup);
        return true;
    }

    @Override
    public List<IElement> getElements() {
        return elements;
    }

    @Override
    public List<IGuiEventListener> getEventListeners() {
        return listeners;
    }
}
