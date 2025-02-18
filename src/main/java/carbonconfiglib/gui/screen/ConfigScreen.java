package carbonconfiglib.gui.screen;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.config.ArrayElement;
import carbonconfiglib.gui.config.CompoundElement;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.FolderElement;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.config.SelectionElement;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.CarbonIconCheckbox;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.Icon;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.fml.ModList;

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
public class ConfigScreen extends ListScreen
{
	private static final Comparator<Element> SORTER = (K, V) -> {
		int sort = (V instanceof FolderElement ? 1 : 0) - (K instanceof FolderElement ? 1 : 0);
		return sort != 0 ? sort : String.CASE_INSENSITIVE_ORDER.compare(K.getName(), V.getName());
	};
	
	Screen parent;
	IModConfig config;
	IConfigNode node;
	CarbonIconCheckbox deepSearch;
	CarbonIconCheckbox onlyChanged;
	CarbonIconCheckbox onlyNonDefault;
	boolean wasChanged = false;
	Navigator nav;
	List<Element> cache = null;
	
	public ConfigScreen(IModConfig config, Screen parent) {
		this(Navigator.create(config), config, parent);
	}
	
	public ConfigScreen(Navigator nav, IModConfig config, Screen parent) {
		this(nav, config, parent, BackgroundTexture.DEFAULT.asHolder());
	}
	
	public ConfigScreen(Navigator nav, IModConfig config, Screen parent, BackgroundHolder customTexture) {
		super(Component.empty(), customTexture);
		this.nav = nav;
		this.config = config;
		this.node = config.getRootNode();
		this.parent = parent;
		this.nav.setScreenForLayer(this);
	}
	
	public ConfigScreen(Navigator nav, IConfigNode node, Screen parent, BackgroundHolder customTexture) {
		super(Component.empty(), customTexture);
		this.nav = nav;
		this.node = node;
		this.parent = parent;
		this.nav.setScreenForLayer(this);
	}
	
	@Override
	protected void init() {
		super.init();
		int x = width / 2 - 100;
		int y = height;
		if(node.isRoot()) {
			addRenderableWidget(new CarbonButton(x-51, y-27, 100, 20, Component.translatable("gui.carbonconfig.save"), this::save));
			addRenderableWidget(new CarbonButton(x+51, y-27, 100, 20, Component.translatable("gui.carbonconfig.reset"), this::reset));
			addRenderableWidget(new CarbonButton(x+153, y-27, 100, 20, Component.translatable("gui.carbonconfig.back"), this::goBack));
		}
		else {
			addRenderableWidget(new CarbonButton(x+101, y-27, 100, 20, Component.translatable("gui.carbonconfig.back"), this::goBack));
			addRenderableWidget(new CarbonButton(x-1, y-27, 100, 20, Component.translatable("gui.carbonconfig.home"), this::goToRoot));
		}
		if(shouldHaveSearch()) {
			deepSearch = addRenderableWidget(new CarbonIconCheckbox(x+205, 25, 20, 20, Icon.SEARCH_SELECTED, Icon.SEARCH, false).withListener(this::onDeepSearch).setTooltip(this, "gui.carbonconfig.deepsearch"));
			onlyChanged = addRenderableWidget(new CarbonIconCheckbox(x+227, 25, 20, 20, Icon.SET_DEFAULT, Icon.REVERT, false).withListener(this::onChangedButton).setTooltip(this, "gui.carbonconfig.changed_only"));
			onlyNonDefault = addRenderableWidget(new CarbonIconCheckbox(x+249, 25, 20, 20, Icon.NOT_DEFAULT_SELECTED, Icon.NOT_DEFAULT, false).withListener(this::onDefaultButton).setTooltip(this, "gui.carbonconfig.default_only"));
		}
		String walkNode = nav.getWalkNode();
		if(walkNode != null) {
			FolderElement element = getElement(walkNode);
			if(element != null) {
				element.onPress(null);
			}
			nav.consumeWalker();
		}
	}
	
	private void onDeepSearch() {
		if(onlyChanged.selected() || onlyNonDefault.selected()) deepSearch.setSelected(false);
		else {
			wasChanged = true;
		}
	}
	
	private void onChangedButton() {
		deepSearch.setSelected(false);
		onlyNonDefault.setSelected(false);
	}
	
	private void onDefaultButton() {
		deepSearch.setSelected(false);
		onlyChanged.setSelected(false);
	}
	
	@Override
	public void tick() {
		super.tick();
		if(shouldHaveSearch() && (onlyChanged.selected() || onlyNonDefault.selected() || wasChanged)) {
			onSearchChange(searchBox, searchBox.getValue().toLowerCase(Locale.ROOT));
			wasChanged = onlyChanged.selected() || onlyNonDefault.selected();
		}
	}
	
	@Override
	public void handleForground(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		GuiUtils.drawScrollingString(stack, font, nav.getHeader(), 50F, 6, width-100, 10, GuiAlign.CENTER, -1, 0);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(mouseX >= 50F && mouseX <= width-100 && mouseY >= 6 && mouseY <= 16) {
			float scroll = GuiUtils.calculateScrollOffset(width-100, font, GuiAlign.CENTER, nav.getHeader(), 0);
			Screen screen = nav.getScreen(font, (int)(mouseX - GuiAlign.CENTER.align(50, width-100, font.width(nav.getHeader())) - scroll));
			if(screen instanceof ConfigScreen) {
				minecraft.setScreen(screen);
				return true;
			}
			else if(screen != null) { 
				leave();
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	protected List<Element> sortElements(List<Element> list) {
		list.sort(SORTER);
		return list;
	}
	
	private void goToRoot(Button button) {
		Screen prev = this;
		Screen parent = this;
		while(parent instanceof ConfigScreen) {
			prev = parent;
			parent = ((ConfigScreen)parent).parent;
		}
		if(prev != this) {
			minecraft.setScreen(prev);
		}
	}
	
	private void leave() {
		ConfigScreen prev = this;
		Screen parent = this;
		while(parent instanceof ConfigScreen) {
			prev = (ConfigScreen)parent;
			parent = ((ConfigScreen)parent).parent;
		}
		if(prev != this) {
			Screen toOpen = prev.parent;
			if(node.isRoot() && prev.isChanged()) {
				minecraft.setScreen(new ConfirmScreen(T -> {
					minecraft.setScreen(T ? toOpen : this);	
				}, Component.translatable("gui.carbonconfig.warn.changed"), Component.translatable("gui.carbonconfig.warn.changed.desc").withStyle(ChatFormatting.GRAY)));
				return;
			}
			minecraft.setScreen(toOpen);
		}
	}
	
	private void reset(Button button) {
		minecraft.setScreen(new MultiChoiceScreen(T -> {
			if(T.isMain()) processAction(IConfigNode::setDefault);
			else if(T.isOther()) processAction(IConfigNode::setPrevious);
			minecraft.setScreen(this);
		}, Component.translatable("gui.carbonconfig.reset_all.title"), Component.translatable("gui.carbonconfig.reset_all.message").withStyle(ChatFormatting.GRAY), 
			Component.translatable("gui.carbonconfig.reset_all.default"), Component.translatable("gui.carbonconfig.reset_all.reset"), Component.translatable("gui.carbonconfig.reset_all.cancel")));
	}
	
	private void save(Button button) {
		List<IConfigNode> value = processedChanged(IConfigNode::save);
		config.save();
		if(findFirst(IConfigNode::requiresRestart, value)) {
			MultiChoiceScreen choice = new MultiChoiceScreen(T -> {
				minecraft.setScreen(parent);
			}, Component.translatable("gui.carbonconfig.restart.title"), Component.translatable("gui.carbonconfig.restart.message").withStyle(ChatFormatting.GRAY), Component.translatable("gui.carbonconfig.ok"));
			minecraft.setScreen(choice);
			return;
		}
		else if(minecraft.level != null && findFirst(IConfigNode::requiresReload, value)) {
			MultiChoiceScreen choice = new MultiChoiceScreen(T -> {
				minecraft.setScreen(parent);
			}, Component.translatable("gui.carbonconfig.reload.title"), Component.translatable("gui.carbonconfig.reload.message").withStyle(ChatFormatting.GRAY), Component.translatable("gui.carbonconfig.ok"));
			minecraft.setScreen(choice);
			return;
		}
		minecraft.setScreen(parent);
	}
	
	private <T> boolean findFirst(Predicate<T> filter, List<T> elements) {
		for(int i = 0,m=elements.size();i<m;i++) {
			if(filter.test(elements.get(i))) return true;
		}
		return false;
	}
	
	private List<IConfigNode> processedChanged(Consumer<IConfigNode> action) {
		List<IConfigNode> output = new ObjectArrayList<>();
		PriorityQueue<IConfigNode> nodes = new ObjectArrayFIFOQueue<>();
		nodes.enqueue(node);
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.dequeue();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::enqueue);
				continue;
			}
			if(node.isChanged()) {
				action.accept(node);
				output.add(node);
			}
		}
		return output;
	}
	
	private void processAction(Consumer<IConfigNode> action) {
		PriorityQueue<IConfigNode> nodes = new ObjectArrayFIFOQueue<>();
		nodes.enqueue(node);
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.dequeue();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::enqueue);
				continue;
			}
			action.accept(node);
		}
	}
	
	@Override
	protected void onSearchChange(EditBox box, String value) {
		if((!deepSearch.selected() || value.isEmpty()) && !onlyChanged.selected() && !onlyNonDefault.selected()) {
			super.onSearchChange(box, value);
			return;
		}
		if(cache == null) {
			cache = sortElements(ConfigScreen.getAllElements(node));
			cache.forEach(this::addInternal);
		}
		if(onlyNonDefault.selected()) {
			List<Element> subCache = new ObjectArrayList<>();
			for(Element element : cache) {
				if(!element.isDefault()) subCache.add(element);
			}
			super.onSearchChange(box, value, subCache);
			return;
		}
		if(onlyChanged.selected()) {
			List<Element> subCache = new ObjectArrayList<>();
			for(Element element : cache) {
				if(element.isChanged()) subCache.add(element);
			}
			super.onSearchChange(box, value, subCache);
			return;
		}
		super.onSearchChange(box, value, cache);
	}
	
	private void goBack(Button button) {
		if(node.isRoot() && isChanged()) {
			minecraft.setScreen(new ConfirmScreen(T -> {
				minecraft.setScreen(T ? parent : this);	
			}, Component.translatable("gui.carbonconfig.warn.changed"), Component.translatable("gui.carbonconfig.warn.changed.desc").withStyle(ChatFormatting.GRAY)));
			return;
		}
		minecraft.setScreen(parent);
	}
	
	private boolean isChanged() {
		PriorityQueue<IConfigNode> nodes = new ObjectArrayFIFOQueue<>();
		nodes.enqueue(node);
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.dequeue();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::enqueue);
				continue;
			}
			if(node.isChanged()) return true;
		}
		return false;
	}
	
	@Override
	protected int getListWidth() {
		return 340;
	}
	
	@Override
	protected int getScrollPadding() {
		return 175;
	}
		
	@Override
	protected void collectElements(Consumer<Element> elements) {
		for(IConfigNode child : node.getChildren()) {
			if(child.isLeaf()) {
				switch(child.getDataStructure()) {
					case COMPOUND:
						elements.accept(new CompoundElement(INode.asCompound(child)));
						break;
					case LIST:
						elements.accept(new ArrayElement(INode.asArray(child)));
						break;
					case SIMPLE:
						IValueNode node = INode.asValue(child);
						if(node == null) break;
						if(node.isForcingSuggestions()) {
							elements.accept(new SelectionElement(node));
							break;
						}
						Element element = node.getDataType().create(node);
						if(element != null) elements.accept(element);
						break;
				}
			}
			else elements.accept(new FolderElement(child, nav));
		}
	}
	
	private static List<Element> getAllElements(IConfigNode init) {
		PriorityQueue<IConfigNode> nodes = new ObjectArrayFIFOQueue<>();
		nodes.enqueue(init);
		List<Element> results = new ObjectArrayList<>();
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.dequeue();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::enqueue);
				continue;
			}
			switch(node.getDataStructure()) {
				case COMPOUND:
					results.add(new CompoundElement(INode.asCompound(node)));
					break;
				case LIST:
					results.add(new ArrayElement(INode.asArray(node)));
					break;
				case SIMPLE:
					IValueNode value = INode.asValue(node);
					if(value == null) break;
					if(value.isForcingSuggestions()) {
						results.add(new SelectionElement(value));
						break;
					}
					Element element = value.getDataType().create(value);
					if(element != null) results.add(element);
					break;
			}
		}
		return results;
	}
	
	public FolderElement getElement(String name) {
		for(Element element : allEntries) {
			if(element instanceof FolderElement) {
				FolderElement folder = (FolderElement)element;
				if(folder.getNode() != null && name.equalsIgnoreCase(folder.getNode().getNodeName())) {
					return folder;
				}
			}
		}
		return null;
	}
	
	public static class Navigator {
		private static final Component SPLITTER = Component.literal(" > ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
		List<Component> layer = new ObjectArrayList<>();
		List<Screen> screenByIndex = new ObjectArrayList<>();
		List<String> walker = null;
		MutableComponent buildCache = null;
		
		private Navigator() {}
		
		public Navigator(Component base) {
			layer.add(base);
		}
		
		public static Navigator create(IModConfig config) {
			Navigator nav = new Navigator(Component.literal(ModList.get().getModContainerById(config.getModId()).map(T -> T.getModInfo().getDisplayName()).orElse("Unknown")));
			nav.setScreenForLayer(null);
			return nav.add(Component.translatable("gui.carbonconfig.type."+config.getConfigType().name().toLowerCase()));
		}
		
		public Navigator add(Component name) {
			return add(name, null);
		}
		
		public Navigator add(Component name, String walkerEntry) {
			Navigator nav = new Navigator();
			nav.layer.addAll(layer);
			nav.screenByIndex.addAll(screenByIndex);
			nav.layer.add(name);
			if(walker != null && walker.size() > 1 && walkerEntry != null && walker.indexOf(walkerEntry.toLowerCase(Locale.ROOT)) == 0) {
				nav.walker = new ObjectArrayList<>();
				for(int i = 1;i<walker.size();i++) {
					nav.walker.add(walker.get(i));
				}
			}
			return nav;
		}
		
		public Navigator withWalker(String...traversePath) {
			walker = new ObjectArrayList<>();
			for(String path : traversePath) {
				walker.add(path.toLowerCase(Locale.ROOT));
			}
			return this;
		}
		
		public void setScreenForLayer(Screen owner) {
			if(layer.size() > screenByIndex.size()) screenByIndex.add(owner);
			else screenByIndex.set(layer.size()-1, owner);
		}
		
		public Screen getScreen(Font font, int x) {
			int splitterWidth = font.width(SPLITTER);
			for(int i = 0,m=layer.size();i<m;i++) {
				int width = font.width(layer.get(i));
				if(x >= 0 && x <= width) return screenByIndex.get(i);
				x-=width;
				x-=splitterWidth;
			}
			return null;
		}
		
		protected void consumeWalker() {
			walker = null;
		}
		
		protected String getWalkNode() {
			return walker == null ? null : walker.get(0);
		}
		
		public Component getHeader() {
			if(buildCache == null) {
				buildCache = Component.empty();
				for(int i = 0,m=layer.size();i<m;i++) {
					buildCache.append(layer.get(i));
					if(i == m-1) continue;
					buildCache.append(SPLITTER);
				}
			}
			return buildCache;
		}
	}
}