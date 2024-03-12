package carbonconfiglib.gui.impl.carbon;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.IReloadMode;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.util.text.ITextComponent;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.utils.Stack;

public class CarbonValue implements IValueNode, IValueActions
{
	IReloadMode mode;
	ITextComponent name;
	ITextComponent tooltip;
	DataType type;
	boolean forced;
	Supplier<List<Suggestion>> suggestions;
	
	Function<String, ParseResult<Boolean>> isValid;
	Consumer<String> saveAction;
	
	Stack<String> previous = new ObjectArrayList<>();
	String current;
	String defaultValue;
	
	public CarbonValue(IReloadMode mode, ITextComponent name, ITextComponent tooltip, DataType type, boolean forced, Supplier<List<Suggestion>> suggestions, String current, String defaultValue, Function<String, ParseResult<Boolean>> isValid, Consumer<String> saveAction) {
		this.mode = mode;
		this.name = name;
		this.tooltip = tooltip;
		this.type = type;
		this.forced = forced;
		this.suggestions = suggestions;
		this.isValid = isValid;
		this.saveAction = saveAction;
		this.current = current;
		this.defaultValue = defaultValue;
		previous.push(current);
	}

	public void save() { saveAction.accept(current); }
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
	public void set(String value) { this.current = value; }
	@Override
	public ParseResult<Boolean> isValid(String value) { return isValid.apply(value); }
	@Override
	public DataType getDataType() { return type; }
	@Override
	public boolean isForcingSuggestions() { return forced; }
	@Override
	public List<Suggestion> getSuggestions() { return suggestions.get(); }
}
