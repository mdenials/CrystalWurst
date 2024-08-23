/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.events.UpdateListener;

@SearchTags({"quickcraft", "mass craft"})
public final class MassCraftHack extends Hack implements UpdateListener
{
	public MassCraftHack()
	{
		super("MassCraft");
		setCategory(Category.ITEMS);
	}


	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}


	@Override
	public void onUpdate()
	{
		ClientTickEvents.START_CLIENT_TICK.register((MinecraftClient minecraftClient)-> {craft();});
	}

	public static void craft()
	{
		MinecraftClient client = MinecraftClient.getInstance();
   		ClientPlayerEntity ply = client.player;
    		ClientPlayerInteractionManager im = client.interactionManager;
    		if (im == null || ply == null) return;
    		PlayerInventory inv = ply.getInventory();
    		AbstractRecipeScreenHandler<CraftingScreenHandler, Integer> rsh = getRecipeScreenHandler();
    		if (rsh == null) return;
    		int resultSlotIndex = rsh.getCraftingResultSlotIndex();
    		ItemStack outStack = rsh.slots.get(resultSlotIndex).getStack();
    		if (Screen.hasAltDown() || !hasSpace(inv, outStack)) ply.dropSelectedItem(true);
    		im.clickSlot(0, resultSlotIndex, 0, SlotActionType.QUICK_MOVE, ply);
	}


	public static AbstractRecipeScreenHandler<CraftingScreenHandler, 0> getRecipeScreenHandler()
	{
		ClientPlayerEntity ply = MinecraftClient.getInstance().player;
    		return ply == null 
		? null : ply.currentScreenHandler instanceof CraftingScreenHandler || ply.currentScreenHandler instanceof PlayerScreenHandler 
	    	? (AbstractRecipeScreenHandler<CraftingScreenHandler, Integer>) ply.currentScreenHandler : null;
	}

	public static boolean hasSpace(PlayerInventory inv, ItemStack outStack)
	{
    		return outStack.isEmpty() || inv.getEmptySlot() >= 0 || inv.getOccupiedSlotWithRoomForStack(outStack) >= 0;
	}
}
