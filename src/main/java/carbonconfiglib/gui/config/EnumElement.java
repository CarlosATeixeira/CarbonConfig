package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.screen.EditStringScreen;
import carbonconfiglib.gui.screen.ListSelectionScreen;
import carbonconfiglib.gui.screen.ListSelectionScreen.NodeSupplier;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

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
public class EnumElement extends ConfigElement
{
	ParseResult<Boolean> result;
	
	public EnumElement(IValueNode value) {
		super(value);
	}
	
	public EnumElement(IArrayNode array, IValueNode value) {
		super(array, value);
	}
	
	public EnumElement(ICompoundNode compound, IValueNode value) {
		super(compound, value);
	}
	
	@Override
	public void init() {
		super.init();
		if(!hasSuggestions() || isArray()) {
			if(this.isArray()) {
				addChild(new CarbonButton(0, 0, 40, 18, I18n.format("gui.carbonconfig.edit"), this::onSelect), -32);				
			}
			else {
				addChild(new CarbonButton(0, 0, 72, 18, I18n.format("gui.carbonconfig.edit"), this::onPress));
			}
		}
	}
	
	@Override
	protected int getMaxX(int prevMaxX) {
		return super.getMaxX(prevMaxX) - 140;
	}
	
	@Override
	public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		super.render(x, top, left, width, height, mouseX, mouseY, selected, partialTicks);
		String value = this.value.get();
		if(isCompound()) {
			int offset = font.getStringWidth(value) + 135;
			GuiUtils.drawScrollingString(font, value, left + width - offset, top, 135, height - 2.75F, GuiAlign.LEFT, -1, 0);			
		}
		else if(isArray()) {
			GuiUtils.drawScrollingString(font, value, left + (canMove() ? 5 : 10), top, 140, height - 2.75F, GuiAlign.LEFT, -1, 0);
		}
		else {
			GuiUtils.drawScrollingString(font, value, left - 20, top, 140, height - 2.75F, GuiAlign.LEFT, -1, 0);
		}
	}
	
	private void onSelect(Button button) {
		mc.displayGuiScreen(new ListSelectionScreen(mc.currentScreen, value, NodeSupplier.ofValue(), owner.getCustomTexture()));
	}
	
	private void onPress(Button button) {
		mc.displayGuiScreen(new EditStringScreen(mc.currentScreen, name, value, owner.getCustomTexture()));
	}
}
