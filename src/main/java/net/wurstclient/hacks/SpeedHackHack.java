/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"speed hack"})
public final class SpeedHackHack extends Hack implements UpdateListener
{
	public final SliderSetting speed = new SliderSetting("Horizontal Speed", 0.66, 0, 20, 0.000001, ValueDisplay.DECIMAL);
	public final SliderSetting jumpscale = new SliderSetting("Jump Scale", 0.1, 0, 20, 0.000001, ValueDisplay.DECIMAL);
	public final SliderSetting power = new SliderSetting("Power", 1, 1, 38, 1, ValueDisplay.INTEGER);
	private final CheckboxSetting onGround = new CheckboxSetting("Ground check", true);
	
	public SpeedHackHack()
	{
		super("SpeedHack");
		setCategory(Category.MOVEMENT);
		addSetting(speed);
		addSetting(jumpscale);
        	addSetting(power);
		addSetting(onGround);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// return if sneaking or not walking
		if(MC.player.isSneaking() || MC.player.forwardSpeed == 0 && MC.player.sidewaysSpeed == 0)
			return;
		
		// activate sprint if walking forward
		if(MC.player.forwardSpeed > 0 && !MC.player.horizontalCollision)
			MC.player.setSprinting(true);

		// activate mini jump if on ground
		if(!MC.player.isOnGround() && onGround.isChecked())
			return;

		double spv = Math.pow(speed.getValue(), power.getValueI());
		Vec3d v = MC.player.getVelocity();
		MC.player.setVelocity(v.x * spv, v.y + jumpscale.getValue(), v.z * spv);
		
		v = MC.player.getVelocity();
		double currentSpeed = Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.z, 2));
		
		// limit speed to highest value that works on NoCheat+ version
		// 3.13.0-BETA-sMD5NET-b878
		// UPDATE: Patched in NoCheat+ version 3.13.2-SNAPSHOT-sMD5NET-b888
		double maxSpeed = Math.pow(speed.getValue(), power.getValueI());
		
		if(currentSpeed > maxSpeed)
			MC.player.setVelocity(v.x / currentSpeed * maxSpeed, v.y, v.z / currentSpeed * maxSpeed);
	}
}
