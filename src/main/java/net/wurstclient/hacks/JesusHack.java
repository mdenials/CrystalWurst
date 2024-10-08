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

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.BlockListSetting;

@SearchTags({"WaterWalking", "water walking"})
public final class JesusHack extends Hack implements UpdateListener, PacketOutputListener
{
	private final SliderSetting liquidVelocity = new SliderSetting("Water velocity", "Water up velocity.\n", 0.005, 0, 20, 0.000001, ValueDisplay.DECIMAL);
	private final SliderSetting waterJump = new SliderSetting("Water jump", "Water jump simulate value.\n", 0.3, 0, 20, 0.000001, ValueDisplay.DECIMAL);
	private final SliderSetting playerOffset = new SliderSetting("PlayerOffset", "Sets the player offset.\n", -0.5, -20, 20, 0.000001, ValueDisplay.DECIMAL);
	private final SliderSetting tick = new SliderSetting("Timer", "Jesus tick timer.\n", 10, 0, 20, 1, ValueDisplay.INTEGER);
	private final SliderSetting packetT = new SliderSetting("Packet Timer", "Packet timer.\n", 4, 0, 20, 1, ValueDisplay.INTEGER);
	private final SliderSetting dst = new SliderSetting("Distance", "Water bypass distance.\n", 0.05, 0, 20, 0.000001, ValueDisplay.DECIMAL);
	private final BlockListSetting liquids = new BlockListSetting("Liquids", "The Liquids blocks");
	private final BlockListSetting solids = new BlockListSetting("Solids", "The solids blocks");
	private final CheckboxSetting solidliquid = new CheckboxSetting("Solid liquid", "Make liquid is solid.", false);
	private final CheckboxSetting bypass = new CheckboxSetting("NoCheat+ bypass", "Bypasses NoCheat+ but slows down your movement.", false);
	
	private int tickTimer = 10;
	private int packetTimer = 0;
	
	public JesusHack()
	{
		super("Jesus");
		setCategory(Category.MOVEMENT);
		addSetting(liquidVelocity);
        	addSetting(waterJump);
        	addSetting(playerOffset);
        	addSetting(tick);
        	addSetting(packetT);
        	addSetting(dst);
		addSetting(liquids);
		addSetting(solids);
        	addSetting(solidliquid);
		addSetting(bypass);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(PacketOutputListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PacketOutputListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// check if sneaking
		if(MC.options.sneakKey.isPressed())
			return;
		
		ClientPlayerEntity player = MC.player;
		
		// move up in liquid
		if(player.isTouchingWater() || player.isInLava())
		{
			Vec3d velocity = player.getVelocity();
			player.setVelocity(velocity.x, (double)(liquidVelocity.getValue()), velocity.z);
			tickTimer = 0;
			return;
		}
		
		// simulate jumping out of water
		Vec3d velocity = player.getVelocity();
		if(tickTimer == 0)
			player.setVelocity(velocity.x, (double)(waterJump.getValue()), velocity.z);
		else if(tickTimer == 1)
			player.setVelocity(velocity.x, 0, velocity.z);
		
		// update timer
		tickTimer++;
	}
	
	@Override
	public void onSentPacket(PacketOutputEvent event)
	{
		// check packet type
		if(!(event.getPacket() instanceof PlayerMoveC2SPacket))
			return;
		
		PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket)event.getPacket();
		
		// check if packet contains a position
		if(!(packet instanceof PlayerMoveC2SPacket.PositionAndOnGround
			|| packet instanceof PlayerMoveC2SPacket.Full))
			return;
		
		// check inWater
		if(MC.player.isTouchingWater())
			return;
		
		// check fall distance
		if(MC.player.fallDistance > 3F)
			return;
		
		if(!isOverLiquid())
			return;
		
		// if not actually moving, cancel packet
		if(MC.player.input == null)
		{
			event.cancel();
			return;
		}
		
		// wait for timer
		packetTimer++;
		if(packetTimer < packetT.getValueI())
			return;
		
		// cancel old packet
		event.cancel();
		
		// get position
		double x = packet.getX(0);
		double y = packet.getY(0);
		double z = packet.getZ(0);
		
		// offset y
		if(bypass.isChecked() && MC.player.age % 2 == 0)
			y -= (double)(dst.getValue());
		else
			y += (double)(dst.getValue());
		
		// create new packet
		Packet<?> newPacket;
		if(packet instanceof PlayerMoveC2SPacket.PositionAndOnGround)
			newPacket =
				new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true);
		else
			newPacket = new PlayerMoveC2SPacket.Full(x, y, z, packet.getYaw(0),
				packet.getPitch(0), true);
		
		// send new packet
		MC.player.networkHandler.getConnection().send(newPacket);
	}
	
	public boolean isOverLiquid()
	{
		boolean foundLiquid = false;
		boolean foundSolid = false;
		Box box = MC.player.getBoundingBox().offset(0, (double)(playerOffset.getValue()), 0);
		
		// check collision boxes below player
		ArrayList<Block> blockCollisions = BlockUtils.getBlockCollisions(box)
			.map(bb -> BlockUtils.getBlock(BlockPos.ofFloored(bb.getCenter())))
			.collect(Collectors.toCollection(ArrayList::new));
		
		for(Block block : blockCollisions)
		{
			if(liquids.contains(block))
				foundLiquid = true;
				
			if(solids.contains(block))
				foundSolid = true;

			/* if(block instanceof FluidBlock)
				foundLiquid = true;
			else if(!(block instanceof AirBlock))
				foundSolid = true; */
		}

		return foundLiquid && !foundSolid;
	}
	
	public boolean shouldBeSolid()
	{
		return isEnabled() && MC.player != null && MC.player.fallDistance <= 3
			&& !MC.options.sneakKey.isPressed() && !MC.player.isTouchingWater()
			&& !MC.player.isInLava() && solidliquid.isChecked();
	}
}
