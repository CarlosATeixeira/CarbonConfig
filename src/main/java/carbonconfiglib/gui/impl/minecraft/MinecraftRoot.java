package carbonconfiglib.gui.impl.minecraft;

import java.util.List;
import java.util.Map;

import carbonconfiglib.gui.api.IConfigFolderNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.impl.minecraft.MinecraftConfig.Category;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
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
public class MinecraftRoot implements IConfigFolderNode
{
	Map<Category, List<IGameRuleValue>> configNodes;
	List<IConfigNode> children;
	
	public MinecraftRoot(Map<Category, List<IGameRuleValue>> configNodes) {
		this.configNodes = configNodes;
	}
	
	@Override
	public List<IConfigNode> getChildren() {
		if(children == null) {
			children = new ObjectArrayList<>();
			for(Map.Entry<Category, List<IGameRuleValue>> entry : configNodes.entrySet()) {
				children.add(new MinecraftFolder(entry));
			}
		}
		return children;
	}
	@Override
	public String getNodeName() { return null; }
	@Override
	public IChatComponent getName() { return new ChatComponentText("Minecraft"); }
	@Override
	public boolean isRoot() { return true; }
}
