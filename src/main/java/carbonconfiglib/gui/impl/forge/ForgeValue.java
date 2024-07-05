package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.text.ITextComponent;

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
public class ForgeValue implements IValueNode
{
	ITextComponent name;
	ITextComponent tooltip;
	DataType type;
	ReloadMode mode;
	Function<String, ParseResult<?>> isValid;
	Supplier<List<Suggestion>> suggestions;
	BiConsumer<String, ForgeValue> saved;	
	
	ObjectArrayList<String> previous = new ObjectArrayList<>();
	String current;
	String defaultValue;
	
	public ForgeValue(ITextComponent name, ITextComponent tooltip, ReloadMode mode, DataType type, String value, String defaultValue, Supplier<List<Suggestion>> suggestions, Function<String, ParseResult<?>> isValid, BiConsumer<String, ForgeValue> saved) {
		this.name = name;
		this.tooltip = tooltip;
		this.isValid = isValid;
		this.mode = mode;
		this.type = type;
		this.current = value;
		previous.push(current);
		this.defaultValue = defaultValue;
		this.suggestions = suggestions;
		this.saved = saved;
	}

	public void save() { saved.accept(current, this); }
	@Override
	public boolean isDefault() { return Objects.equals(defaultValue, current); }
	@Override
	public boolean isChanged() { return !Objects.equals(previous.top(), current); }
	@Override
	public void setDefault() { current = defaultValue; }
	@Override
	public void setPrevious() {
		current = previous.top();
		if(previous.size() > 1) previous.pop();
	}
	@Override
	public void createTemp() { previous.push(current); }
	@Override
	public void apply() {
		if(previous.size() > 1) previous.pop();
	}
	@Override
	public StructureType getNodeType() { return StructureType.SIMPLE; }
	@Override
	public boolean requiresRestart() { return mode == ReloadMode.GAME; }
	@Override
	public boolean requiresReload() { return mode == ReloadMode.WORLD; }
	@Override
	public ITextComponent getName() { return name; }
	@Override
	public ITextComponent getTooltip() { return tooltip; }
	@Override
	public String get() { return current; }
	@Override
	public void set(String value) { current = value; }
	@Override
	public ParseResult<Boolean> isValid(String value) { return ParseResult.success(isValid.apply(value).isValid()); }
	@Override
	public DataType getDataType() { return type; }
	@Override
	public boolean isForcingSuggestions() { return type == DataType.ENUM; }
	@Override
	public List<Suggestion> getSuggestions() { return suggestions.get(); }
}
