/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

public final class TimerHack extends Hack
{
	private final SliderSetting speed = new SliderSetting("Speed", 2, 0, 20, 0.000001, ValueDisplay.DECIMAL);
	public final SliderSetting power = new SliderSetting("Power", 1, 0, 38, 1, ValueDisplay.INTEGER);
	
	public TimerHack()
	{
		super("Timer");
		setCategory(Category.OTHER);
		addSetting(speed);
		addSetting(power);
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + " [" + speed.getValueString() + "]";
	}
	
	public float getTimerSpeed()
	{
		return isEnabled() ? (float)Math.pow(speed.getValueF(), power.getValueI()) : 1;
	}
}
