package carbonconfiglib.networking;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import carbonconfiglib.networking.carbon.ConfigAnswerPacket;
import carbonconfiglib.networking.carbon.ConfigRequestPacket;
import carbonconfiglib.networking.carbon.SaveConfigPacket;
import carbonconfiglib.networking.carbon.StateSyncPacket;
import carbonconfiglib.networking.forge.RequestConfigPacket;
import carbonconfiglib.networking.forge.SaveForgeConfigPacket;
import carbonconfiglib.networking.minecraft.RequestGameRulesPacket;
import carbonconfiglib.networking.minecraft.SaveGameRulesPacket;
import carbonconfiglib.networking.snyc.BulkSyncPacket;
import carbonconfiglib.networking.snyc.SyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.sets.ObjectOpenHashSet;

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
public class CarbonNetwork
{
	public static final String VERSION = "1.0.0";
	Set<UUID> clientInstalledPlayers = new ObjectOpenHashSet<>();
	boolean serverInstalled = false;
	
	public void init(RegisterPayloadHandlerEvent event) {
		IPayloadRegistrar type = event.registrar("carbonconfig").optional().versioned(VERSION);
		type.play(SyncPacket.ID, readPacket(SyncPacket::new), this::handlePacket);
		type.play(BulkSyncPacket.ID, readPacket(BulkSyncPacket::new), this::handlePacket);
		type.play(ConfigRequestPacket.ID, readPacket(ConfigRequestPacket::new), this::handlePacket);
		type.play(ConfigAnswerPacket.ID, readPacket(ConfigAnswerPacket::new), this::handlePacket);
		type.play(SaveConfigPacket.ID, readPacket(SaveConfigPacket::new), this::handlePacket);
		type.play(RequestConfigPacket.ID, readPacket(RequestConfigPacket::new), this::handlePacket);
		type.play(SaveForgeConfigPacket.ID, readPacket(SaveForgeConfigPacket::new), this::handlePacket);
		type.play(RequestGameRulesPacket.ID, readPacket(RequestGameRulesPacket::new), this::handlePacket);
		type.play(SaveGameRulesPacket.ID, readPacket(SaveGameRulesPacket::new), this::handlePacket);
		type.play(StateSyncPacket.ID, readPacket(StateSyncPacket::new), this::handlePacket);
	}
		
	protected <T extends ICarbonPacket> FriendlyByteBuf.Reader<T> readPacket(Function<FriendlyByteBuf, T> provider) {
		return B -> {
			try { return provider.apply(B); }
			catch(Exception e) { e.printStackTrace(); }
			return null;
		};
	}
	
	protected void handlePacket(ICarbonPacket packet, IPayloadContext provider) {
		try {
			provider.workHandler().execute(() -> packet.process(getPlayer(provider)));
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public boolean isInWorld() {
		return getClientPlayer() != null;
	}
	
	protected Player getPlayer(IPayloadContext cont) {
		Optional<Player> entity = cont.player();
		return entity.isPresent() ? entity.get() : getClientPlayer();
	}
	
	@OnlyIn(Dist.CLIENT)
	protected Player getClientPlayer() {
		Minecraft mc = Minecraft.getInstance();
		return mc == null ? null : mc.player;
	}
	
	public void sendToServer(ICarbonPacket packet) {
		PacketDistributor.SERVER.noArg().send(packet);
	}
	
	public void sendToAllPlayers(ICarbonPacket packet) {
		for(ServerPlayer player : getAllPlayers()) {
			PacketDistributor.PLAYER.with(player).send(packet);
		}
	}
	
	public void onPlayerJoined(Player player, boolean server) {
		if(server) clientInstalledPlayers.add(player.getUUID());
		else serverInstalled = true;
	}
	
	public void onPlayerLeft(Player player, boolean server) {
		if(server) clientInstalledPlayers.remove(player.getUUID());
		else serverInstalled = false;
	}
	
	private List<ServerPlayer> getAllPlayers() {
		List<ServerPlayer> players = new ObjectArrayList<>();
		for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
			if(isInstalledOnClient(player)) 
				players.add(player);
		}
		return players;
	}
	
	public boolean isInstalled(Player player) {
		return player instanceof ServerPlayer ? isInstalledOnClient((ServerPlayer)player) : isInstalledOnServer();
	}
	
	public boolean isInstalledOnClient(ServerPlayer player) {
		return clientInstalledPlayers.contains(player.getUUID());
	}
		
	public boolean isInstalledOnServer() {
		return serverInstalled;
	}
	
	public void sendToPlayer(ICarbonPacket packet, Player player) {
		if(!(player instanceof ServerPlayer)) {
			throw new RuntimeException("Sending a Packet to a Player from client is not allowed");
		}
		PacketDistributor.PLAYER.with((ServerPlayer)player).send(packet);
	}
}
