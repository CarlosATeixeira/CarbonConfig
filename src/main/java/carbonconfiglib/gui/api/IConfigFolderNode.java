package carbonconfiglib.gui.api;

import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.network.chat.Component;

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
public interface IConfigFolderNode extends IConfigNode
{
	@Override
	default INode asNode() { return null; }
	@Override
	default StructureType getDataStructure() { return null; }
	@Override
	public default boolean isLeaf() { return false; }
	@Override
	public default boolean isRoot() { return false; }
	@Override
	public default boolean isChanged() { return false; }
	@Override
	public default void save() {}
	@Override
	public default void setPrevious() {}
	@Override
	public default void setDefault() {}
	@Override
	public default boolean requiresRestart() { return false; }
	@Override
	public default boolean requiresReload() { return false; }
	@Override
	public default Component getTooltip() { return Component.empty(); }
}
