package carbonconfiglib.networking.forge;

import java.io.ByteArrayInputStream;

import com.electronwill.nightconfig.toml.TomlFormat;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.impl.forge.ForgeHelpers;
import carbonconfiglib.networking.ICarbonPacket;
import carbonconfiglib.utils.Helpers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;

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
public class SaveForgeConfigPacket implements ICarbonPacket
{
    public static final StreamCodec<FriendlyByteBuf, SaveForgeConfigPacket> STREAM_CODEC = CustomPacketPayload.codec(SaveForgeConfigPacket::write, ICarbonPacket.readPacket(SaveForgeConfigPacket::new));
	public static final CustomPacketPayload.Type<SaveForgeConfigPacket> ID = ICarbonPacket.createType("carbonconfig:save_neo");
	ModConfig.Type type;
	String modId;
	byte[] data;
	
	public SaveForgeConfigPacket(ModConfig.Type type, String modId, byte[] data) {
		this.type = type;
		this.modId = modId;
		this.data = data;
	}
	
	public SaveForgeConfigPacket(FriendlyByteBuf buffer) {
		type = buffer.readEnum(ModConfig.Type.class);
		modId = buffer.readUtf(32767);
		data = buffer.readByteArray();
	}
	
	public void write(FriendlyByteBuf buffer) {
		buffer.writeEnum(type);
		buffer.writeUtf(modId, 32767);
		buffer.writeByteArray(data);
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() { return ID; }
	
	@Override
	public void process(Player player) {
		if(!CarbonConfig.hasPermission(player, 4)) {
			return;
		}
		ModConfig config = findConfig();
		if(config == null) return;
		ForgeHelpers.saveConfig(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(data)), config);
		CarbonConfig.LOGGER.info("Saved ["+modId+"] "+Helpers.firstLetterUppercase(type.extension())+" Config");
	}
	
	private ModConfig findConfig() {
		for(ModConfig config : ConfigTracker.INSTANCE.configSets().get(type)) {
			if(modId.equalsIgnoreCase(config.getModId())) return config;
		}
		return null;
	}
}
