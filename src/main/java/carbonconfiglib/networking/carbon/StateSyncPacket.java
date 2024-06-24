package carbonconfiglib.networking.carbon;


import carbonconfiglib.CarbonConfig;
import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.networking.ICarbonPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;

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
public class StateSyncPacket implements ICarbonPacket
{
    public static final StreamCodec<FriendlyByteBuf, StateSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(StateSyncPacket::write, ICarbonPacket.readPacket(StateSyncPacket::new));
	public static final CustomPacketPayload.Type<StateSyncPacket> ID = CustomPacketPayload.createType("carbonconfig:state");
	Dist source;
	
	public StateSyncPacket(Dist source) {
		this.source = source;
	}
	
	public StateSyncPacket(FriendlyByteBuf buffer) {
		source = buffer.readBoolean() ? Dist.CLIENT : Dist.DEDICATED_SERVER;
	}
	
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(source.isClient());
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() { return ID; }
	
	@Override
	public void process(Player player) {
		if(source.isDedicatedServer()) CarbonConfig.NETWORK.onPlayerJoined(player, false);
		else EventHandler.INSTANCE.onServerJoinPacket(player);
	}
	
}
