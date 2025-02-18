package carbonconfiglib.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.widgets.CarbonEditBox;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;

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
public class ColorElement extends ConfigElement
{
	CarbonEditBox textBox;
	ParseResult<Boolean> result;
	
	public ColorElement(IValueNode value) {
		super(value);
	}
	
	public ColorElement(IArrayNode array, IValueNode value) {
		super(array, value);
	}
	
	public ColorElement(ICompoundNode compound, IValueNode value) {
		super(compound, value);
	}
	
	@Override
	public void init() {
		super.init();
		textBox = addChild(new CarbonEditBox(font, 0, 0, isArray() ? 130 : (isCompound() ? 85 : 52), 18).setInnerDiff(4), isArray() ? GuiAlign.CENTER : GuiAlign.RIGHT, 1);
		textBox.setValue(value.get());
		textBox.setResponder(T -> {
			textBox.setTextColor(0xE0E0E0);
			result = null;
			if(!T.isEmpty()) {
				result = value.isValid(T);
				if(!result.getValue()) {
					textBox.setTextColor(0xFF0000);
					return;
				}
				value.set(T);
			}
		});
	}
	
	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		super.render(poseStack, x, top, left, width, height, mouseX, mouseY, selected, partialTicks);
		try {
			if(isArray()) {
				GuiComponent.fill(poseStack, left+186, top-1, left+203, top+19, 0xFFA0A0A0);
				GuiComponent.fill(poseStack, left+187, top, left+202, top+18, (int)(Long.decode(value.get()) | 0xFF000000L));
			}
			else {
				int xOff = isCompound() ? 194 : 207;
				GuiComponent.fill(poseStack, left+xOff, top-1, left+xOff+17, top+19, 0xFFA0A0A0);
				GuiComponent.fill(poseStack, left+xOff+1, top, left+xOff+16, top+18, (int)(Long.decode(value.get()) | 0xFF000000L));
			}
		}
		catch(Exception e) {}
		if(textBox.isMouseOver(mouseX, mouseY) && result != null && !result.getValue()) {
			owner.addTooltips(Component.literal(result.getError().getMessage()).withStyle(ChatFormatting.RED));
		}
	}
	
	@Override
	public void updateValues() {
		textBox.setValue(value.get());
	}
}
