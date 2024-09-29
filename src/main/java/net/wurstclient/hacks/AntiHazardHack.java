/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.stream.Collectors;

import net.minecraft.block.Block;

import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.BlockCollisionShapeListener;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.BlockUtils;

@SearchTags({"NoCactus", "anti cactus", "no cactus"})
public final class AntiHazardHack extends Hack implements BlockCollisionShapeListener
{
    public final BlockListSetting hazards = new BlockListSetting("Hazard List", "The types of hazards blocks to isolate");

	public AntiHazardHack()
	{
		super("AntiHazard");
		setCategory(Category.BLOCKS);
		addSetting(hazards);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(BlockCollisionShapeListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(BlockCollisionShapeListener.class, this);
	}
	
	@Override
	public void onBlockCollisionShape(BlockCollisionShapeEvent event)
	{
        	if(hazards.contains(event.getCollisionBlock()))
			event.setCollisionShape(VoxelShapes.fullCube());
	}
}
