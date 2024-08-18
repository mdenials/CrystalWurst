/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Random;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.BlockBreakingProgressListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockUtils;

@SearchTags({"FastMine", "SpeedMine", "SpeedyGonzales", "fast break",
	"fast mine", "speed mine", "speedy gonzales", "NoBreakDelay",
	"no break delay"})
public final class FastBreakHack extends Hack
	implements UpdateListener, BlockBreakingProgressListener
{
	private final EnumSetting<Mode> mode = new EnumSetting<>("Mode", "Fastbreak modes", Mode.values(), Mode.PROGRESS);
	private final SliderSetting breakProgress = new SliderSetting("Break Progress", 1, 0, 1, 0.000001, ValueDisplay.PERCENTAGE);
	
	private final SliderSetting activationChance = new SliderSetting(
		"Activation chance",
		"Only FastBreaks some of the blocks you break with the given chance,"
			+ " which makes it harder for anti-cheat plugins to detect.",
		1, 0, 1, 0.01, ValueDisplay.PERCENTAGE);
	
	private final CheckboxSetting zeroDelay = new CheckboxSetting("Zero Delay",
		"Only removes the delay between breaking blocks, without speeding up the breaking process itself.",
		false);
	
	private final Random random = new Random();
	private BlockPos lastBlockPos;
	private boolean fastBreakBlock;
	
	public FastBreakHack()
	{
		super("FastBreak");
		setCategory(Category.BLOCKS);
		addSetting(mode);
		addSetting(breakBrogress);
		addSetting(activationChance);
		addSetting(zeroDelay);
	}
	
	@Override
	public String getRenderName()
	{
		if(legitMode.isChecked())
			return getName() + "Legit";
		return getName();
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(BlockBreakingProgressListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(BlockBreakingProgressListener.class, this);
		lastBlockPos = null;
	}
	
	@Override
	public void onUpdate()
	{
		if(zeroDelay.isChecked()) MC.interactionManager.blockBreakingCooldown = 0;
	}
	
	@Override
	public void onBlockBreakingProgress(BlockBreakingProgressEvent event)
	{
		BlockPos blockPos = event.getBlockPos();
		Direction direction = event.getDirection();
		float bProgress = breakProgress.getValueF();
		// Ignore unbreakable blocks to avoid slowdown issue
		if(BlockUtils.isUnbreakable(blockPos))
			return;
		
		if(!blockPos.equals(lastBlockPos))
		{
			lastBlockPos = blockPos;
			fastBreakBlock = random.nextDouble() <= activationChance.getValue();
		}
		
		if(!fastBreakBlock)
			return;

		if(mode.getSelected() == Mode.PROGRESS)
		{
			if (MC.interactionManager.currentBreakingProgress >= bProgress)
				MC.interactionManager.currentBreakingProgress = 1F;
		}
		
		if(mode.getSelected() == Mode.PACKET)
		{
			IMC.getInteractionManager().sendPlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction);
			IMC.getInteractionManager().sendPlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction);
		}	
	}
	
 	private enum Mode
	{
		PROGRESS("Progress"),
		PACKET("Packet");
		
		private final String name;
		
		private Mode(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
