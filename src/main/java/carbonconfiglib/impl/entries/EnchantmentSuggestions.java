package carbonconfiglib.impl.entries;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.api.ISuggestionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class EnchantmentSuggestions implements ISuggestionProvider
{
	public static final ISuggestionProvider INSTANCE = new EnchantmentSuggestions();
	
	@Override
	public void provideSuggestions(Consumer<Suggestion> output, Predicate<Suggestion> filter) {
		for(ResourceLocation key : getEnchantments()) {
			Suggestion suggestion = Suggestion.namedTypeValue(key.toString(), key.toString(), Enchantment.class);
			if(filter.test(suggestion)) output.accept(suggestion);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	private List<ResourceLocation> getEnchantments() {
		Level level = Minecraft.getInstance().level;
		if(level == null) return getDefaults();
		Registry<Enchantment> registry = level.registryAccess().registry(Registries.ENCHANTMENT).orElse(null);
		return registry == null ? null : new ObjectArrayList<>(registry.keySet());
	}
	
	private List<ResourceLocation> getDefaults() {
		List<ResourceLocation> enchantments = new ObjectArrayList<>();
		enchantments.add(Enchantments.PROTECTION.location());
		enchantments.add(Enchantments.FIRE_PROTECTION.location());
		enchantments.add(Enchantments.FEATHER_FALLING.location());
		enchantments.add(Enchantments.BLAST_PROTECTION.location());
		enchantments.add(Enchantments.PROJECTILE_PROTECTION.location());
		enchantments.add(Enchantments.RESPIRATION.location());
		enchantments.add(Enchantments.AQUA_AFFINITY.location());
		enchantments.add(Enchantments.THORNS.location());
		enchantments.add(Enchantments.DEPTH_STRIDER.location());
		enchantments.add(Enchantments.FROST_WALKER.location());
		enchantments.add(Enchantments.BINDING_CURSE.location());
		enchantments.add(Enchantments.SOUL_SPEED.location());
		enchantments.add(Enchantments.SWIFT_SNEAK.location());
		enchantments.add(Enchantments.SHARPNESS.location());
		enchantments.add(Enchantments.SMITE.location());
		enchantments.add(Enchantments.BANE_OF_ARTHROPODS.location());
		enchantments.add(Enchantments.KNOCKBACK.location());
		enchantments.add(Enchantments.FIRE_ASPECT.location());
		enchantments.add(Enchantments.LOOTING.location());
		enchantments.add(Enchantments.SWEEPING_EDGE.location());
		enchantments.add(Enchantments.EFFICIENCY.location());
		enchantments.add(Enchantments.SILK_TOUCH.location());
		enchantments.add(Enchantments.UNBREAKING.location());
		enchantments.add(Enchantments.FORTUNE.location());
		enchantments.add(Enchantments.POWER.location());
		enchantments.add(Enchantments.PUNCH.location());
		enchantments.add(Enchantments.FLAME.location());
		enchantments.add(Enchantments.INFINITY.location());
		enchantments.add(Enchantments.LUCK_OF_THE_SEA.location());
		enchantments.add(Enchantments.LURE.location());
		enchantments.add(Enchantments.LOYALTY.location());
		enchantments.add(Enchantments.IMPALING.location());
		enchantments.add(Enchantments.RIPTIDE.location());
		enchantments.add(Enchantments.CHANNELING.location());
		enchantments.add(Enchantments.MULTISHOT.location());
		enchantments.add(Enchantments.QUICK_CHARGE.location());
		enchantments.add(Enchantments.PIERCING.location());
		enchantments.add(Enchantments.DENSITY.location());
		enchantments.add(Enchantments.BREACH.location());
		enchantments.add(Enchantments.WIND_BURST.location());
		enchantments.add(Enchantments.MENDING.location());
		enchantments.add(Enchantments.VANISHING_CURSE.location());
		return enchantments;
	}
}