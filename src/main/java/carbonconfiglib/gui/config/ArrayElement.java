package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.screen.ArrayScreen;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.CarbonIconButton;
import net.minecraft.client.gui.GuiButton;
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
public class ArrayElement extends ConfigElement
{
	GuiButton textBox = addChild(new CarbonButton(0, 0, 72, 18, I18n.format("gui.carbonconfig.edit"), this::onPress));
	IArrayNode node;
	
	public ArrayElement(IArrayNode node) {
		super(node.getName());
		this.node = node;
	}
	
	public ArrayElement(IArrayNode owner, IArrayNode node) {
		super(owner, node.getName());
		this.node = node;
	}
	
	public ArrayElement(ICompoundNode owner, IArrayNode node) {
		super(owner, node.getName());
		this.node = node;
	}
	
	private void onPress(GuiButton button) {
		mc.displayGuiScreen(new ArrayScreen(node, mc.currentScreen, owner.getCustomTexture()));
	}
	
	@Override
	protected boolean createResetButtons(IValueNode value) {
		return true;
	}
	
	@Override
	protected int indexOf() {
		return array.indexOf(node);
	}
	
	@Override
	protected boolean isReset() {
		return node.isChanged();
	}
	
	@Override
	public boolean isChanged() {
		return node.isChanged();
	}
	
	@Override
	public boolean isDefault() {
		return node.isDefault();
	}
	
	@Override
	protected void onDefault(CarbonIconButton button) {
		node.setDefault();
		updateValues();
	}
	
	@Override
	protected void onReset(CarbonIconButton button) {
		node.setPrevious();
		updateValues();
	}
}
