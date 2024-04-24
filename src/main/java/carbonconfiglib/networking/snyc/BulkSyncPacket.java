package carbonconfiglib.networking.snyc;

import java.util.Collection;
import java.util.List;

import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.networking.CarbonNetwork;
import carbonconfiglib.networking.ICarbonPacket;
import carbonconfiglib.utils.SyncType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

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
public class BulkSyncPacket implements ICarbonPacket
{
    public static final StreamCodec<FriendlyByteBuf, BulkSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(BulkSyncPacket::write, CarbonNetwork.readPacket(BulkSyncPacket::new));
	public static final Type<BulkSyncPacket> ID = CustomPacketPayload.createType("carbonconfig:bulksync");
	List<SyncPacket> packets = new ObjectArrayList<>();
	
	public BulkSyncPacket(List<SyncPacket> packets) {
		this.packets = packets;
	}
	
	public BulkSyncPacket(FriendlyByteBuf buffer) {
		int size = buffer.readVarInt();
		for(int i = 0;i<size;i++) {
			packets.add(new SyncPacket(buffer));
		}
	}
	
	public static BulkSyncPacket create(Collection<ConfigHandler> toSync, SyncType type, boolean forceSync) {
		List<SyncPacket> result = new ObjectArrayList<>();
		for(ConfigHandler handler : toSync) {
			SyncPacket packet = SyncPacket.create(handler, type, forceSync);
			if(packet != null) result.add(packet);
		}
		return result.isEmpty() ? null : new BulkSyncPacket(result);
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(packets.size());
		for(SyncPacket packet : packets) {
			packet.write(buffer);
		}
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() { return ID; }
	
	@Override
	public void process(Player player) {
		ReloadMode result = null;
		for(SyncPacket packet : packets) {
			result = ReloadMode.or(result, packet.processEntry(player));
		}
		if(result != null) {
			player.sendSystemMessage(result.getMessage());
		}
	}
	
}
