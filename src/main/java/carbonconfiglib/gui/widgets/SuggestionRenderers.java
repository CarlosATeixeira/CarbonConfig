package carbonconfiglib.gui.widgets;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.api.ISuggestionRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.registries.ForgeRegistries;

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
public class SuggestionRenderers
{
	public static class ItemEntry implements ISuggestionRenderer {
		@Override
		public Component renderSuggestion(GuiGraphics graphics, String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryParse(value);
			if(id == null) return null;
			Item item = ForgeRegistries.ITEMS.getValue(id);
			if(item == Items.AIR || item == null) return null;
			ItemStack itemStack = new ItemStack(item);
			graphics.renderItem(itemStack, x, y);
			return itemStack.getHoverName().copy().withStyle(ChatFormatting.YELLOW).append("\n").append(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));			
		}
	}
	
	public static class FluidEntry implements ISuggestionRenderer {
		@Override
		public Component renderSuggestion(GuiGraphics graphics, String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryParse(value);
			if(id == null) return null;
			Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
			if(fluid == Fluids.EMPTY || fluid == null) return null;
			TextureAtlasSprite sprite = getSprite(fluid);
			if(sprite == null) return null;
			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
			int color = IClientFluidTypeExtensions.of(fluid).getTintColor();
			RenderSystem.setShaderColor((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1F);
			graphics.blit(x, y, 0, 18, 18, sprite);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			return fluid.getFluidType().getDescription().copy().withStyle(ChatFormatting.YELLOW).append("\n").append(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));
		}
		
		private TextureAtlasSprite getSprite(Fluid fluid) {
			return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of(fluid).getStillTexture());
		}
	}
	
	public static class EnchantmentEntry implements ISuggestionRenderer {
		@Override
		public Component renderSuggestion(GuiGraphics graphics, String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryParse(value);
			if(id == null) return null;
			ClientLevel level = Minecraft.getInstance().level;
			if(level == null) {
				if(CarbonConfig.SHOW_MISSING_ENCHANTMENT_TEXTURE.get()) {
					graphics.blit(x, y, 0, 18, 18, Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation()));
					return Component.translatable("gui.carbonconfig.enchantment.missing").withStyle(ChatFormatting.RED);
				}
				return null;
			}
			Holder<Enchantment> holder = getEnchantmnetById(level, id);
			if(holder == null) return null;
			Enchantment ench = holder.value();
			if(ench == null) return null;
			graphics.renderItem(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, ench.getMinLevel())), x, y);
			return Enchantment.getFullname(holder, ench.getMinLevel()).copy().withStyle(ChatFormatting.YELLOW).append("\n").append(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));
		}
		
		private Holder<Enchantment> getEnchantmnetById(ClientLevel level, ResourceLocation id) {
			try { return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, id)); }
			catch(Exception e) { return null; }
		}
	}
	
	public static class PotionEntry implements ISuggestionRenderer {
		@Override
		public Component renderSuggestion(GuiGraphics graphics, String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryParse(value);
			if(id == null) return null;
			MobEffect potion = BuiltInRegistries.MOB_EFFECT.get(id);
			if(potion == null) return null;
			ItemStack item = new ItemStack(Items.POTION);
			item.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(potion.getColor()), List.of(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(potion)))));
			graphics.renderItem(item, x, y);
			return potion.getDisplayName().copy().withStyle(ChatFormatting.YELLOW).append("\n").append(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));
		}
	}
	
	public static class ColorEntry implements ISuggestionRenderer {
		@Override
		public Component renderSuggestion(GuiGraphics graphics, String value, int x, int y) {
			try {
				graphics.fill(x+1, y+-1, x+18, y+17, 0xFFA0A0A0);
				graphics.fill(x+2, y, x+17, y+16, Long.decode(value).intValue() | 0xFF000000);
			}
			catch(Exception e) {
			}
			return null;
		}
	}
}
