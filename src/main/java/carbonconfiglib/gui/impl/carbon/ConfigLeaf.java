package carbonconfiglib.gui.impl.carbon;

import java.util.List;

import carbonconfiglib.api.IReloadMode;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigEntry.ParsedArray;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ConfigLeaf implements IConfigNode
{
	ConfigEntry<?> entry;
	IStructuredData data;
	StructureType type;
	IReloadMode mode;
	IValueActions value;
	
	public ConfigLeaf(ConfigEntry<?> entry) {
		this.entry = entry;
		this.data = entry.getDataType();
		this.type = data.getDataType();
		this.mode = entry.getReloadState();
	}
	
	@Override
	public INode asNode() {
		if(value == null) {
			switch(type) {
				case COMPOUND:
					value = new CarbonCompound(mode, data.asCompound(), getName(), getTooltip(), entry.serialize(), entry.serializeDefault(), entry::canSetValue, () -> entry.getSuggestions(T -> true), this::save);
					break;
				case LIST:
					value = new CarbonArray(mode, data.asList(), getName(), getTooltip(), entry.serialize(), entry.serializeDefault(), entry::canSetValue, () -> entry.getSuggestions(T -> true), this::save);
					break;
				case SIMPLE:
					value = new CarbonValue(mode, getName(), getTooltip(), DataType.bySimple(entry.getDataType().asSimple()), entry.areSuggestionsForced(), () -> entry.getSuggestions(T -> true), entry.serialize(), entry.serializeDefault(), entry::canSetValue, this::save);
					break;
			}
		}
		return value;
	}
	
	private void save(String value, IValueActions actions) {
		if(entry instanceof ParsedArray) {
			entry.deserializeValue(Helpers.removeLayer(value, 0));
			return;
		}
		entry.deserializeValue(value);
	}
	
	@Override
	public List<IConfigNode> getChildren() { return null; }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isChanged() { return value != null && value.isChanged(); }
	@Override
	public void setPrevious() {
		if(value != null) value.setPrevious();
	}
	@Override
	public void setDefault() {
		if(value != null) value.setDefault();		
	}
	@Override
	public void save() {
		if(value != null) value.save();
	}
	@Override
	public boolean requiresRestart() { return mode == ReloadMode.GAME; }
	@Override
	public boolean requiresReload() { return mode == ReloadMode.WORLD; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public IChatComponent getName() { return IConfigNode.createLabel(entry.getKey()); }
	@Override
	public IChatComponent getTooltip() {
		ChatComponentText comp = new ChatComponentText("");
		comp.appendSibling(new ChatComponentText(entry.getKey()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
		String[] array = entry.getComment();
		if(array != null && array.length > 0) {
			for(int i = 0;i<array.length;comp.appendText("\n").appendText(array[i++]).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
		}
		String limit = entry.getLimitations();
		if(limit != null && !limit.trim().isEmpty()) comp.appendText("\n").appendSibling(new ChatComponentTranslation(limit).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLUE)));
		return comp;
	}
	@Override
	public StructureType getDataStructure() { return type; }
}
