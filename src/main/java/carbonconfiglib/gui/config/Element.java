package carbonconfiglib.gui.config;

import java.util.List;

import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.screen.AbstractScrollList;
import carbonconfiglib.gui.widgets.screen.IInteractable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import speiger.src.collections.objects.utils.ObjectLists;

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
public class Element extends AbstractScrollList.ContainerEntry<Element> {
	protected Minecraft mc = Minecraft.getMinecraft();
	protected FontRenderer font = mc.fontRendererObj;
	protected IChatComponent name;
	protected IChatComponent unchanged;
	protected IChatComponent changed;
	protected IListOwner owner;
	protected int hash = hashCode();
	
	public Element(IChatComponent name) {
		setName(name);
	}
	
	public String getName() {
		return name.getFormattedText();
	}
	
	public void setName(IChatComponent newName) {
		this.name = newName;
		this.unchanged = name.createCopy().setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
		this.changed = name.createCopy().setChatStyle(new ChatStyle().setColor(EnumChatFormatting.ITALIC));
	}
		
	public void updateValues() {
	}
	
	public boolean isChanged() {
		return false;
	}
	
	public boolean isDefault() {
		return false;
	}
	
	public void init() {
	}
	
	public void tick() {
	}
	
	@Override
	public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
	}
	
	protected void renderName(float x, float y, boolean changed, int width, int height) {
		GuiUtils.drawScrollingString(font, (changed ? this.changed : unchanged).getFormattedText(), x, y-1, width, height, GuiAlign.LEFT, -1, hash);
	}
	
	protected void renderText(IChatComponent text, float x, float y, float width, float height, GuiAlign align, int color) {
		GuiUtils.drawScrollingString(font, text.getFormattedText(), x, y, width, height, align, -1, hash);
	}
	
	@Override
	public List<? extends IInteractable> children() {
		return ObjectLists.empty();
	}
}