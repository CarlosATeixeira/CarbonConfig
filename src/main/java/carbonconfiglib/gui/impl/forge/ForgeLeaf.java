package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Locale;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
import net.minecraftforge.fml.common.Loader;
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
	Property property;
	ForgeValue value;
	ForgeArray array;
	
	public ForgeLeaf(Property property) {
		this.property = property;
	}

	@Override
	public List<IConfigNode> getChildren() { return null; }
	
	@Override
	public INode asNode() {
		if(property.isList()) {
			if(array == null) array = new ForgeArray(getName(), getTooltip(), getMode(), fromType(property.getType()), new ObjectArrayList<>(property.getStringList()), new ObjectArrayList<>(property.getDefaults()), () -> getValidValues(), this::isValid, T -> property.setValues(T.toArray(new String[T.size()])));
			return array;
		}
		if(value == null) value = new ForgeValue(getName(), getTooltip(), getMode(), fromType(property.getType()), property.getString(), property.getDefault(), () -> getValidValues(), this::isValid, (K, V) -> property.set(K));
		return value;
	}
	
	private ReloadMode getMode() {
		return property.requiresMcRestart() ? ReloadMode.GAME : (property.requiresWorldRestart() ? ReloadMode.WORLD : null);
	}
		
	public List<Suggestion> getValidValues() {
		String[] values = property.getValidValues();
		if(values == null || values.length <= 0) return ObjectLists.empty();
		List<Suggestion> suggestion = new ObjectArrayList<>();
		for(String value : values) {
			suggestion.add(Suggestion.value(value));
		}
		return suggestion;
	}
	
	public ParseResult<Boolean> isValid(String value) {
		switch(property.getType()) {
			case BOOLEAN: return ParseResult.success(true);
			case COLOR: return validate(ColorWrapper.parseInt(value));
			case DOUBLE: return validate(Helpers.parseDouble(value));
			case INTEGER: return validate(Helpers.parseInt(value));
			case MOD_ID: return ParseResult.result(Loader.instance().getIndexedModList().containsKey(value), NullPointerException::new, "Mod ["+value+"] isn't a thing");
			case STRING: return ParseResult.success(true);
			default: return ParseResult.success(true);
		}
	}
	
	private ParseResult<Boolean> validate(ParseResult<?> value) {
		return value.hasError() ? value.withDefault(false) : ParseResult.success(true);
	}
	
	@Override
	public StructureType getDataStructure() { return property.isList() ? StructureType.LIST : StructureType.SIMPLE; }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isChanged() { return (value != null && value.isChanged()) || (array != null && array.isChanged()); }
	
	@Override
	public void setPrevious() {
		if(value != null) value.setPrevious();
		if(array != null) array.setPrevious();
	}
	
	@Override
	public void setDefault() {
		asNode();
		if(property.isList()) array.setDefault();
		else value.setDefault();
	}
	
	@Override
	public void save() {
		if(value != null) value.save();
		if(array != null) array.save();
	}
	
	@Override
	public boolean requiresRestart() {
		return property.requiresMcRestart();
	}
	
	@Override
	public boolean requiresReload() {
		return property.requiresWorldRestart();
	}
	
	@Override
	public String getNodeName() {
		return property.getName().toLowerCase(Locale.ROOT);
	}
	
	@Override
	public ITextComponent getName() { return IConfigNode.createLabel(I18n.hasKey(property.getLanguageKey()) ? I18n.format(property.getLanguageKey()) : property.getName()); }
	@Override
	public ITextComponent getTooltip() {
		TextComponentBase comp = new TextComponentString("");
		comp.appendSibling(new TextComponentString(I18n.hasKey(property.getLanguageKey()) ? I18n.format(property.getLanguageKey()) : property.getName()).setStyle(new Style().setColor(TextFormatting.YELLOW)));
		String comment = property.getComment();
		if(comment != null) {
			String[] array = comment.split("\n");
			if(array != null && array.length > 0) {
				for(int i = 0;i<array.length;comp.appendText("\n").appendText(array[i++]).setStyle(new Style().setColor(TextFormatting.GRAY)));
			}
		}
		return comp;
	}
	
	private DataType fromType(Type type) {
		switch(type) {
			case BOOLEAN: return DataType.BOOLEAN;
			case COLOR: return DataType.INTEGER;
			case DOUBLE: return DataType.DOUBLE;
			case INTEGER: return DataType.INTEGER;
			case MOD_ID: return DataType.STRING;
			case STRING: return DataType.STRING;
			default: return DataType.STRING;
		}
	}
}
