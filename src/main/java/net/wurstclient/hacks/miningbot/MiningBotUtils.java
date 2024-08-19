/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.miningbot;

import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.WurstClient;

public enum MiningBotUtils
{
	;
	
	public static boolean isLog(BlockPos pos)
	{
        	return WurstClient.INSTANCE.getHax().miningBotHack.oresList.getBlockNames().contains(BlockUtils.getName(pos)); 
	}

    	public static boolean isLeaves(BlockPos pos)
	{
        	//BlockState state = BlockUtils.getState(pos); return !state.isAir();
        	return WurstClient.INSTANCE.getHax().miningBotHack.filterList.getBlockNames().contains(BlockUtils.getName(pos)); 
	}
}
