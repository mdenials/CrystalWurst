/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.wurstclient.WurstClient;

/**
 * A utility class to turn right-clicking a block into a simple one-liner,
 * without sacrificing anti-cheat resistance or customizability.
 *
 * <p>
 * Accurately replicates {@link MinecraftClient#doItemUse()} as of 1.20.2, while
 * being much easier to read and adding convenient ways to change parts of the
 * behavior.
 */
public enum InteractionSimulator
{
	;
	
	private static final MinecraftClient MC = WurstClient.MC;
	
	public static void rightClickBlock(BlockHitResult hitResult)
	{
		for(Hand hand : Hand.values())
		{
			ItemStack stack = MC.player.getStackInHand(hand);
			if(interactBlockAndSwing(hitResult, hand, stack))
				return;
			
			if(interactItemAndSwing(stack, hand))
				return;
		}
	}
	
	private static boolean interactBlockAndSwing(BlockHitResult hitResult, Hand hand, ItemStack stack)
	{
		// save old stack size and call interactBlock()
		int oldCount = stack.getCount();
		ActionResult result = MC.interactionManager.interactBlock(MC.player, hand, hitResult);
		
		// swing hand and reset equip animation
		if(result.isAccepted())
		{			
			if(!stack.isEmpty() && (stack.getCount() != oldCount
				|| MC.interactionManager.hasCreativeInventory()))
				MC.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
		}
		
		return result != ActionResult.PASS;
	}
	
	/**
	 * Calls {@code interactItem()} and swings the hand if the game would
	 * normally do that.
	 *
	 * @return {@code true} if this call should consume the click and prevent
	 *         any further block/item interactions
	 */
	private static boolean interactItemAndSwing(ItemStack stack, Hand hand)
	{
		// pass if hand is empty
		if(stack.isEmpty())
			return false;
		
		// call interactItem()
		ActionResult result =
			MC.interactionManager.interactItem(MC.player, hand);
				
		// reset equip animation
		if(result.isAccepted())
		{
			MC.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
			return true;
		}
		
		return false;
	}
}
