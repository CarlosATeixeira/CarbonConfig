package carbonconfiglib.examples;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.IEntryDataType.CompoundDataType;
import carbonconfiglib.utils.IEntryDataType.EntryDataType;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.world.item.DyeColor;

public class FullTestCase
{
	public void init(boolean perWorld) {
		Config config = new Config("unittest");
		ConfigSection simple = config.add("simple-entries");
		
		simple.addBool("Flag", false);
		simple.addBool("Commented Flag", false, "Multi", "Comment", "Example");
		simple.addBool("Hidden Flag", false).setHidden();
		simple.addBool("Suggested Flag", false).addSuggestions(false, true);
		simple.addBool("Forced Suggestion Flag", false).addSuggestions(false, true).forceSuggestions(true);
		simple.addBool("Named Suggestion Flag", false).addSuggestionProvider(this::suggestsFlags);
		
		simple.addInt("Simple Number", 0);
		simple.addInt("Commented Number", 0, "Multi", "Comment", "Example");
		simple.addInt("Simple Number Range", 50).setRange(0, 100);
		simple.addInt("Suggestion Number", 0).addSuggestionProvider(this::suggestIntRange);
		simple.addInt("Forced Suggestion Number", 0).addSuggestionProvider(this::suggestIntRange).forceSuggestions(true);
		
		simple.addDouble("Simple Decimal", 0.534);
		simple.addDouble("Commented Decimal", 0.1, "Multi", "Comment", "Example");
		simple.addDouble("Simple Decimal Range", 50.121).setRange(25.2323, 75.3232);
		simple.addDouble("Suggestion Decimal", 0.1212).addSuggestionProvider(this::suggestDoubleRange);
		simple.addDouble("Forced Suggestion Decimal", 0.1212).addSuggestionProvider(this::suggestDoubleRange).forceSuggestions(true);
		
		simple.addString("Simple String", "Testing");
		simple.addString("Filtered String", "Requires a . in the sentince").withFilter(T -> T.contains("."));
		simple.addString("Suggested String", "Red").addSuggestions("Green", "Blue");
		simple.addString("Forced Suggested String", "Red").addSuggestions("Green", "Blue").forceSuggestions(true);
		
		simple.addEnum("Simple Enum", DyeColor.BLACK, DyeColor.class);
		simple.addEnum("Commented Enum", DyeColor.BLACK, DyeColor.class, "Requires to be a dye Color");
		
		ConfigSection collection = config.add("collection-entries");
		collection.addArray("Simple Array", new String[] {"One", "Two", "Three", "Four", "Five", "Six"});
		collection.addArray("Commented Array", "Simple Comment");
		collection.addArray("Filtered Array", new String[] {"Requires a . To be present for each entry"}).withFilter(T -> T.contains("."));
		
		collection.addEnumList("Simple Enum List", Collections.emptyList(), DyeColor.class);
		collection.addEnumList("Commented Enum List", Collections.emptyList(), DyeColor.class, "Requires a dye Color");
		
		ConfigSection special = config.add("special-entries");
		special.addParsed("Single Example", new ExampleValue(), ExampleValue.createSerializer());
		special.addParsedArray("Array Example", ExampleValue.createExample(), ExampleValue.createSerializer());
		
		CarbonConfig.CONFIGS.createConfig(config, perWorld ? PerWorldProxy.perWorld() : ConfigSettings.of()).register();
	}
	
	private void suggestIntRange(Consumer<Suggestion> acceptor, Predicate<Suggestion> filter) {
		for(int index : new int[] {1, 5, 12, 20, 50, 100}) {
			Suggestion entry = Suggestion.value(Integer.toString(index));
			if(filter.test(entry)) acceptor.accept(entry);
		}
	}
	
	private void suggestDoubleRange(Consumer<Suggestion> acceptor, Predicate<Suggestion> filter) {
		for(double index : new double[] {1.1, 5, 12.52, 20, 50.1212121, 100}) {
			Suggestion entry = Suggestion.value(Double.toString(index));
			if(filter.test(entry)) acceptor.accept(entry);
		}
	}
	
	private void suggestsFlags(Consumer<Suggestion> acceptor, Predicate<Suggestion> filter) {
		Suggestion entry = Suggestion.namedValue("Yes", "true");
		if(filter.test(entry)) acceptor.accept(entry);
		entry = Suggestion.namedValue("No", "false");
		if(filter.test(entry)) acceptor.accept(entry);
		entry = Suggestion.namedValue("Maybe?", "true");
		if(filter.test(entry)) acceptor.accept(entry);
	}
	
	public static class ExampleValue
	{
		String name;
		int year;
		double fluffyness;
		int favoriteColour;
		DyeColor color;
		boolean value;
		
		public ExampleValue() {
			this("Testing", 2000, 512.2423, 0xFF00FF, DyeColor.BLACK, false);
		}
		
		public ExampleValue(String name, int year, double fluffyness, int favoriteColour, DyeColor color, boolean value) {
			this.name = name;
			this.year = year;
			this.fluffyness = fluffyness;
			this.favoriteColour = favoriteColour;
			this.color = color;
			this.value = value;
		}
		
		public static IConfigSerializer<ExampleValue> createSerializer() {
			CompoundDataType type = new CompoundDataType();
			type.with("Name", EntryDataType.STRING);
			type.withSuggestion("Year", EntryDataType.INTEGER, ISuggestionProvider.array(Suggestion.value("2000"), Suggestion.value("2005"), Suggestion.value("2017"), Suggestion.value("2023")));
			type.with("Fluffyness", EntryDataType.DOUBLE);
			type.withSuggestion("Color", EntryDataType.INTEGER, ISuggestionProvider.array(Suggestion.namedTypeValue("Red", "0xFF0000", ColorWrapper.class), Suggestion.namedTypeValue("Green", "0x00FF00", ColorWrapper.class), Suggestion.namedTypeValue("Blue", "0x0000FF", ColorWrapper.class), Suggestion.namedTypeValue("Black", "0x000000", ColorWrapper.class), Suggestion.namedTypeValue("White", "0xFFFFFF", ColorWrapper.class)));
			type.withSuggestion("Dye", EntryDataType.ENUM, ISuggestionProvider.enums(DyeColor.class));
			type.with("Valid", EntryDataType.BOOLEAN);
			type.forceSuggestions("Dye");
			return IConfigSerializer.noSync(type, new ExampleValue(), ExampleValue::parse, ExampleValue::serialize);
		}
		
		public static List<ExampleValue> createExample() {
			return Collections.singletonList(new ExampleValue());
		}
		
		/*
		 * Parse Function that parses the DataType.
		 */
		public static ParseResult<ExampleValue> parse(String[] value) {
			if(value.length != 6) return ParseResult.error(Helpers.mergeCompound(value), "6 Elements are required");
			if(value[0] == null || value[0].trim().isEmpty()) return ParseResult.error(value[0], "Value [Name] is not allowed to be null/empty");
			ParseResult<Integer> year = Helpers.parseInt(value[1]);
			if(year.hasError()) return year.onlyError("Couldn't parse [Year] argument");
			ParseResult<Double> fluffyness = Helpers.parseDouble(value[2]);
			if(fluffyness.hasError()) return fluffyness.onlyError("Couldn't parse [Fluffyness] argument");
			ParseResult<Integer> color = ColorWrapper.parseInt(value[3]);
			if(color.hasError()) return color.onlyError("Couldn't parse [Colour] argument");
			ParseResult<DyeColor> dye = Helpers.parseEnum(DyeColor.class, value[4]);
			if(dye.hasError()) return dye.onlyError("Couldn't parse [Dye] argument");
			return ParseResult.success(new ExampleValue(value[0], year.getValue(), fluffyness.getValue(), color.getValue(), dye.getValue(), Boolean.parseBoolean(value[5])));
		}
		
		/*
		 * Serialization function that turns the DataType into a string. 
		 */
		public String[] serialize() {
			return new String[] {name, Integer.toString(year), Double.toString(fluffyness), ColorWrapper.serialize(favoriteColour), color.name(), Boolean.toString(value)};
		}
	}
}
