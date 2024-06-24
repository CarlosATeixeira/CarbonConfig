package carbonconfiglib.gui.config;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.screen.SmoothFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

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
public class ElementList extends ContainerObjectSelectionList<Element>
{
	BackgroundHolder customBackground;
	int listWidth = 220;
	int scrollPadding = 124;
	int endY;
	Consumer<Element> callback;
	int lastTick = 0;
	boolean isScrolling;
	SmoothFloat value = new SmoothFloat(0.8F);
	boolean shouldSelect = false;
	
	public ElementList(int width, int height, int screenY, int endY, int itemHeight) {
		super(Minecraft.getInstance(), width, height, screenY, itemHeight);
		this.endY = endY;
	}
	
	@Override
	protected boolean isSelectedItem(int index) {
		return shouldSelect && Objects.equals(this.getSelected(), this.children().get(index));
	}
	
	public void setShouldSelectEntry(boolean value) {
		this.shouldSelect = value;
	}
	
	public void setCallback(Consumer<Element> callback) {
		this.callback = callback;
	}
	
	public void addElement(Element element) {
		addEntry(element);
	}
	
	public void addElements(List<Element> elements) {
		elements.forEach(this::addEntry);
	}
	
	public void updateList(List<Element> elements) {
		super.replaceEntries(elements);
	}
	
	public void removeElement(Element element) {
		this.removeEntry(element);
	}
	
	public int size() {
		return children().size();
	}
	
	@Override
	public void setSelected(Element p_93462_) {
		super.setSelected(p_93462_);
		if(callback != null && getSelected() != null) {
			callback.accept(getSelected());
		}
	}
	
	public void scrollToElement(Element element, boolean center) {
		int index = children().indexOf(element);
		if(index == -1) return;
		scrollToElement(index, center);
	}
	
	public void scrollToSelected(boolean center) {
		if(getSelected() == null) return;
		scrollToElement(getSelected(), center);
	}
	
	public void scrollToElement(int index, boolean center) {
		if(center) {
			index -= (height / itemHeight) / 3;
		}
		setScrollAmount(Math.max(0, index) * this.itemHeight + this.headerHeight);
	}
	
	public void setListWidth(int listWidth) {
		this.listWidth = listWidth;
	}
	
	public void setScrollPadding(int scrollPadding) {
		this.scrollPadding = scrollPadding;
	}
	
	@Override
	public int getRowWidth() {
		return listWidth;
	}
	
	public int getLastTick() {
		return lastTick;
	}
	
	@Override
	protected int getScrollbarPosition() {
		return this.width / 2 + scrollPadding;
	}
	
	@Override
	protected void updateScrollingState(double mouseX, double mouseY, int button) {
		this.isScrolling = button == 0 && mouseX >= (double)this.getScrollbarPosition() && mouseX < (double)(this.getScrollbarPosition() + 6);
		super.updateScrollingState(mouseX, mouseY, button);
	}
	
	@Override
	public void setScrollAmount(double value) {
		setScrollAmount(value, isScrolling);
	}
	
	public void setScrollAmount(double value, boolean force) {
		float actualValue = (float)Mth.clamp(value, 0, getMaxScroll());
		this.value.setTarget(actualValue);
		if(force) this.value.forceFinish();
	}
	
	@Override
	public double getScrollAmount() {
		return isScrolling ? value.getTarget() : value.getValue();
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		isScrolling = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double otherScroll, double scroll) {
		this.setScrollAmount(this.getScrollAmount() - scroll * (double)this.itemHeight * 2);
		return true;
	}
		
	public void tick() {
		lastTick++;
		int max = this.getItemCount();
		for(int i = 0;i < max;++i)
		{
			int j1 = this.getRowTop(i);
			if(j1+itemHeight >= this.getY() && j1 <= this.getBottom()) {
				getEntry(i).tick();
			}
		}
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		value.update(partialTicks);
		super.setScrollAmount(value.getValue());
		super.renderWidget(graphics, mouseX, mouseY, partialTicks);
	}
	
	public void setCustomBackground(BackgroundHolder customBackground) {
		this.customBackground = customBackground;
	}
	
	@Override
	protected void renderListBackground(GuiGraphics graphics) {
		if(customBackground != null) {
			if((minecraft.level == null || !customBackground.shouldDisableInLevel())) {
				renderBackground(getX(), getRight(), getY(), getBottom(), (float)getScrollAmount(), customBackground.getTexture());			
			}
			return;
		}
		super.renderListBackground(graphics);
	}
	
	@Override
	protected void renderDecorations(GuiGraphics graphics, int mouseX, int mouseY) {
		if(customBackground == null) return;
		renderListOverlay(getX(), getRight(), getY(), getBottom(), width, endY, customBackground.getTexture());
	}
	
	public static void renderListOverlay(int x0, int x1, int y0, int y1, int width, int endY, BackgroundTexture texture) {
		Tesselator tes = Tesselator.getInstance();
		BufferBuilder builder = tes.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, texture.getForegroundTexture());
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		int color = texture.getForegroundBrightness();
		builder.addVertex(x0, y0, -100F).setUv(0, y0 / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0 + width, y0, -100F).setUv(width / 32F, y0 / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0 + width, 0F, -100F).setUv(width / 32F, 0F).setColor(color, color, color, 255);
		builder.addVertex(x0, 0F, -100F).setUv(0F, 0F).setColor(color, color, color, 255);
		builder.addVertex(x0, endY, -100F).setUv(0F, endY / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0 + width, endY, -100F).setUv(width / 32F, endY / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0 + width, y1, -100F).setUv(width / 32F, y1 / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0, y1, -100F).setUv(0F, y1 / 32F).setColor(color, color, color, 255);
		BufferUploader.drawWithShader(builder.buildOrThrow());
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		builder = tes.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		builder.addVertex(x0, y0 + 4, 0F).setColor(0, 0, 0, 0);
		builder.addVertex(x1, y0 + 4, 0F).setColor(0, 0, 0, 0);
		builder.addVertex(x1, y0, 0F).setColor(0, 0, 0, 255);
		builder.addVertex(x0, y0, 0F).setColor(0, 0, 0, 255);
		builder.addVertex(x0, y1, 0F).setColor(0, 0, 0, 255);
		builder.addVertex(x1, y1, 0F).setColor(0, 0, 0, 255);
		builder.addVertex(x1, y1 - 4, 0F).setColor(0, 0, 0, 0);
		builder.addVertex(x0, y1 - 4, 0F).setColor(0, 0, 0, 0);
		BufferUploader.drawWithShader(builder.buildOrThrow());
	}
	
	public static void renderBackground(int x0, int x1, int y0, int y1, float scroll, BackgroundTexture texture) {
		Tesselator tes = Tesselator.getInstance();
		BufferBuilder builder = tes.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, texture.getBackgroundTexture());
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		int color = texture.getBackgroundBrightness();
		builder.addVertex(x0, y1, 0F).setUv(x0 / 32F, (y1 + scroll) / 32F).setColor(color, color, color, 255);
		builder.addVertex(x1, y1, 0F).setUv(x1 / 32F, (y1 + scroll) / 32F).setColor(color, color, color, 255);
		builder.addVertex(x1, y0, 0F).setUv(x1 / 32F, (y0 + scroll) / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0, y0, 0F).setUv(x0 / 32F, (y0 + scroll) / 32F).setColor(color, color, color, 255);
		BufferUploader.drawWithShader(builder.buildOrThrow());
	}
}