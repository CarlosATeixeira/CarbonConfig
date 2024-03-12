package carbonconfiglib.gui.impl.forge;

import java.util.List;

import org.apache.logging.log4j.util.Strings;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.common.collect.Iterables;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.impl.forge.ForgeDataType.EnumDataType;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.ValueSpec;
import speiger.src.collections.objects.lists.ObjectArrayList;
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
public class ForgeLeaf implements IConfigNode
{
	ConfigValue<?> data;
	CommentedConfig config;
	ValueSpec spec;
	ForgeDataType<?> type;
	boolean isArray;
	ForgeValue value;
	ForgeArray array;
	ITextComponent tooltip;
	
	public ForgeLeaf(ForgeConfigSpec spec, ConfigValue<?> data, CommentedConfig config) {
		this.data = data;
		this.config = config;
		this.spec = getSpec(spec, data);
		String[] array = buildComment(spec);
		if(array != null && array.length > 0) {
			StringTextComponent comp = new StringTextComponent("");
			for(int i = 0;i<array.length;comp.appendText("\n").appendText(array[i++]).applyTextStyle(TextFormatting.GRAY));
			tooltip = comp;
		}
		guessDataType();
	}
	
	private void guessDataType() {
		Class<?> clz = spec.getClazz();
		if(clz == Object.class) {
			clz = spec.getDefault().getClass();
		}
		type = ForgeDataType.getDataByType(clz);
		if(type == null && clz != null && List.class.isAssignableFrom(clz)) {
			isArray = true;
			type = ForgeDataType.STRING;
		}
	}
	
	@Override
	public List<IConfigNode> getChildren() { return null; }
	
	@Override
	public INode asNode() {
		if(isArray) {
			if(array == null) array = new ForgeArray(getName(), getTooltip(), spec.needsWorldRestart() ? ReloadMode.WORLD : null, type.getDataType(), getCurrent(), getDefault(), () -> ObjectLists.empty(), type::parse, this::save);
			return array;
		}
		if(value == null) value = new ForgeValue(getName(), getTooltip(), spec.needsWorldRestart() ? ReloadMode.WORLD : null, type.getDataType(), getCurrent(type), getDefault(type), this::getSuggestions, type::parse, this::save);
		return value;
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getDefault() {
		return new ObjectArrayList<>((List<String>)spec.getDefault());
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getCurrent() {
		return new ObjectArrayList<>((List<String>)config.get(data.getPath()));
	}
	
	@SuppressWarnings("unchecked")
	private <T> String getDefault(ForgeDataType<T> type) {
		return type.serialize((T)spec.getDefault());
	}
	
	private <T> String getCurrent(ForgeDataType<T> type) {
		return type.isEnum() && String.class.equals(config.get(data.getPath()).getClass()) ? config.get(data.getPath()) : type.serialize(config.get(data.getPath()));
	}
	
	private List<Suggestion> getSuggestions() {
		return type instanceof EnumDataType ? ((EnumDataType<?>)type).getSuggestions(spec) : ObjectLists.empty();
	}
	
	private void save(String value) {
		config.set(data.getPath(), type.parse(value).getValue());
	}
	
	private void save(List<String> values) {
		config.set(data.getPath(), values);
	}
	
	@Override
	public StructureType getDataStructure() { return isArray ? StructureType.LIST : StructureType.SIMPLE; }
	
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	
	@Override
	public boolean isChanged() {
		if(value != null) {
			if(value.isChanged()) return true;
		}
		if(array != null) {
			if(array.isChanged()) return true;
		}
		return false;
	}
	
	@Override
	public void save() {
		if(value != null) value.save();
		if(array != null) array.save();
	}
	
	@Override
	public void setPrevious() {
		if(value != null) value.setPrevious();
		if(array != null) array.setPrevious();
	}
	
	@Override
	public void setDefault() {
		asNode();
		if(isArray) array.setDefault();
		else value.setDefault();
	}
	
	@Override
	public boolean requiresRestart() { return false; }
	@Override
	public boolean requiresReload() { return spec.needsWorldRestart(); }
	@Override
	public String getNodeName() { return null; }
	@Override
	public ITextComponent getName() { return IConfigNode.createLabel(Iterables.getLast(data.getPath(), "")); }
	@Override
	public ITextComponent getTooltip() {
		StringTextComponent comp = new StringTextComponent("");
		comp.appendSibling(new StringTextComponent(Iterables.getLast(data.getPath(), "")).applyTextStyle(TextFormatting.YELLOW));
		if(tooltip != null) comp.appendSibling(tooltip);
		String limit = type.getLimitations(spec);
		if(limit != null && !Strings.isBlank(limit)) comp.appendText("\n").appendSibling(new StringTextComponent(limit).applyTextStyle(TextFormatting.BLUE));
		return comp;
	}
	
	private String[] buildComment(ForgeConfigSpec spec) {
		String value = this.spec.getComment();
		if(value == null) return null;
		int cutoffPoint = getSmallerOfPresent(value.indexOf("Range: "), value.indexOf("Allowed Values: "));
		return (cutoffPoint >= 0 ? value.substring(0, cutoffPoint) : value).split("\n");
	}
	
	private ValueSpec getSpec(ForgeConfigSpec spec, ConfigValue<?> value) {
		return spec.get(value.getPath());
	}
	
	/**
	 * Function that finds the Lowest "present" index of a List/String.
	 * Idea is you try to find the lowest of two "List.indexOf" results.
	 * Since the range of List.indexOf is -1 -> Integer.MAX_VALUE you want anything bigger then -1 if either of them is.
	 * If both of them are -1 means you have non found so -1 can be returned.
	 * 
	 * @author Meduris (Who found the best implementation after a small fun challenge)
	 * 
	 * @param first number to compare
	 * @param second number to compare
	 * @return the highest non -1 number if found. Otherwise it is -1
	 */
	private int getSmallerOfPresent(int first, int second) {
	    if (first == -1 || (second != -1 && first > second))
	        return second;
	    return first;
	}
}