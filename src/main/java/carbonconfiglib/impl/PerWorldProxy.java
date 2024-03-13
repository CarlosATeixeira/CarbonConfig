package carbonconfiglib.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.api.IConfigProxy;
import carbonconfiglib.api.SimpleConfigProxy.SimpleTarget;
import carbonconfiglib.config.ConfigSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
public final class PerWorldProxy implements IConfigProxy
{
	public static final IConfigProxy INSTANCE = new PerWorldProxy(getGameDirectory().resolve("multiplayerconfigs"), getGameDirectory().resolve("defaultconfigs"), getGameDirectory().resolve("saves"));
	Path baseClientPath;
	Path baseServerPath;
	Path saveFolders;
	
	private PerWorldProxy(Path baseClientPath, Path baseServerPath, Path saveFolders) {
		this.baseClientPath = baseClientPath;
		this.baseServerPath = baseServerPath;
		this.saveFolders = saveFolders;
	}
	
	private static Path getGameDirectory() {
		return Loader.instance().getConfigDir().toPath().getParent();
	}
	
	public static boolean isProxy(IConfigProxy proxy) {
		return proxy instanceof PerWorldProxy;
	}
	
	public static ConfigSettings perWorld() {
		return ConfigSettings.withFolderProxy(INSTANCE).withType(ConfigType.SERVER);
	}
	
	@Override
	public Path getBasePaths(Path relativeFile) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server != null) {
			Path path = ((SaveFormatOld)server.getActiveAnvilConverter()).savesDirectory.toPath().resolve(server.getFolderName()).resolve("serverconfig");
			if(Files.exists(path.resolve(relativeFile))) return path;
		}
		else if(FMLCommonHandler.instance().getSide().isClient() && CarbonConfig.NETWORK.isInWorld()) return baseClientPath;
		return baseServerPath;
	}
	
	@Override
	public List<? extends IPotentialTarget> getPotentialConfigs() {
		if(FMLCommonHandler.instance().getSide().isClient()) return getLevels();
		else {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			return Collections.singletonList(new SimpleTarget(((SaveFormatOld)server.getActiveAnvilConverter()).savesDirectory.toPath().resolve(server.getFolderName()).resolve("serverconfig"), "server"));
		}
	}
	
	@SideOnly(Side.CLIENT)
	private List<IPotentialTarget> getLevels() {
		ISaveFormat storage = Minecraft.getMinecraft().getSaveLoader();
		List<IPotentialTarget> folders = new ObjectArrayList<>();
		if(Files.exists(baseServerPath)) {
			folders.add(new SimpleTarget(baseServerPath, "Default Config"));
		}
		try {
			Path basePath = Minecraft.getMinecraft().mcDataDir.toPath().resolve("saves");
			for(SaveFormatComparator sum : storage.getSaveList()) {
				try {
					Path path = basePath.resolve(sum.getFileName()).resolve("level.dat");
					if(Files.exists(path)) folders.add(new WorldTarget(sum, path.getParent(), path));
				}
				catch(Exception e) { e.printStackTrace(); }
			}
		}
		catch(Exception e) { e.printStackTrace(); }
		return folders;
	}
	
	@Override
	public boolean isDynamicProxy() {
		return true;
	}
	
	public static class WorldTarget implements IPotentialTarget {
		SaveFormatComparator summary;
		Path worldFile;
		Path folder;
		
		public WorldTarget(SaveFormatComparator summary, Path worldFile, Path folder) {
			this.summary = summary;
			this.worldFile = worldFile;
			this.folder = folder;
		}

		@Override
		public Path getFolder() {
			return folder;
		}

		@Override
		public String getName() {
			return summary.getDisplayName();
		}
		
		public Path getWorldFile() {
			return worldFile;
		}
		
		public SaveFormatComparator getSummary() {
			return summary;
		}
	}
}
