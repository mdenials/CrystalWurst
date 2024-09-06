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
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"quickcraft", "mass craft"})
public final class MassCraftHack extends Hack implements UpdateListener
{
	private final SliderSetting delay = new SliderSetting("Delay", "Masscrafter delay. ", 5, 0, 20, 1, ValueDisplay.INTEGER);
	
	private int timer;
	public MassCraftHack()
	{
		super("MassCraft");
		setCategory(Category.ITEMS);
		addSetting(delay);
	}


	@Override
	public void onEnable()
	{
		timer = 0;
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
		ClientPlayerEntity ply = MC.player;
    		ClientPlayerInteractionManager im = MC.interactionManager;
    		PlayerInventory inv = MC.player.getInventory();
		
		if (MC.player == null || MC.interactionManager == null)
			return;
		
		// Check if the current screen handler is an instance of CraftingScreenHandler or PlayerScreenHandler
    		if (!(ply.currentScreenHandler instanceof CraftingScreenHandler) && !(ply.currentScreenHandler instanceof PlayerScreenHandler))
        		return;
		
		// wait for timer
		if(timer > 0)
		{
			timer--;
			return;
		}
		timer = delay.getValueI();

    		if (ply.currentScreenHandler instanceof CraftingScreenHandler)
    		{
        		CraftingScreenHandler rsh = (CraftingScreenHandler) ply.currentScreenHandler;
        		int resultSlotIndex = rsh.getCraftingResultSlotIndex();
        		ItemStack outStack = rsh.getSlot(resultSlotIndex).getStack();
			int syncID = ply.currentScreenHandler.syncId;
			
        		if (Screen.hasAltDown() || !hasSpace(inv, outStack)) 
            			ply.dropSelectedItem(true);
			
        		im.clickSlot(syncID, resultSlotIndex, 0, SlotActionType.QUICK_MOVE, ply);
    		} 
    		else if (ply.currentScreenHandler instanceof PlayerScreenHandler)
    		{
        		PlayerScreenHandler psh = (PlayerScreenHandler) ply.currentScreenHandler;
        		int resultSlotIndex = psh.getCraftingResultSlotIndex();
        		ItemStack outStack = psh.getSlot(resultSlotIndex).getStack();
			int syncID = ply.currentScreenHandler.syncId;
			
        		if (Screen.hasAltDown() || !hasSpace(inv, outStack)) 
            			ply.dropSelectedItem(true);
			
        		im.clickSlot(syncID, resultSlotIndex, 0, SlotActionType.QUICK_MOVE, ply);
    		}
	}

	public boolean hasSpace(PlayerInventory inv, ItemStack outStack)
	{
    		if (outStack.isEmpty()) return true;
    		for (int i = 0; i < inv.size(); i++)
		{
        		ItemStack stack = inv.getStack(i);
        		if (stack.isEmpty() || (stack.getItem() == outStack.getItem() && stack.getCount() < stack.getMaxCount()))
			{
            			return true;
        		}
   		}
    		return false;
	}
}
