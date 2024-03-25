package carbonconfiglib.gui.widgets;

import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Copyright 2023 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CarbonButton extends Button
{
	private static final WidgetSprites SPRITES = new WidgetSprites(new ResourceLocation("widget/button"), new ResourceLocation("widget/button_disabled"), new ResourceLocation("widget/button_highlighted"));
	int hash;

	public CarbonButton(int i, int j, int k, int l, Component component, OnPress onPress, CreateNarration createNarration) {
		super(i, j, k, l, component, onPress, createNarration);
		hash = component.getString().hashCode();
	}

	public CarbonButton(int i, int j, int k, int l, Component component, OnPress onPress) {
		this(i, j, k, l, component, onPress, DEFAULT_NARRATION);
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
	    graphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
		GuiUtils.drawScrollingShadowString(graphics, mc.font, getMessage(), getX(), getY(), width, height-2, GuiAlign.CENTER, this.active ? 16777215 : 10526880, hash);
	}
}
