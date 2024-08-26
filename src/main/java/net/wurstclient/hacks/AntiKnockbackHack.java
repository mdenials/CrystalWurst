/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Random;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.KnockbackListener;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"anti knockback", "AntiVelocity", "anti velocity", "NoKnockback",
	"no knockback", "AntiKB", "anti kb"})
public final class AntiKnockbackHack extends Hack implements KnockbackListener, PacketInputListener
{
	private final SliderSetting hStrength =
		new SliderSetting("Horizontal Strength",
			"How far to reduce horizontal knockback.\n"
				+ "100% = no knockback\n" + ">100% = reverse knockback",
			1, 0.01, 2, 0.01, ValueDisplay.PERCENTAGE);
	
	private final SliderSetting vStrength =
		new SliderSetting("Vertical Strength",
			"How far to reduce vertical knockback.\n" + "100% = no knockback\n"
				+ ">100% = reverse knockback",
			1, 0, 2, 0.000001, ValueDisplay.PERCENTAGE);
	
	private final SliderSetting activationChance = new SliderSetting("Activation chance", 1, 0, 1, 0.000001, ValueDisplay.PERCENTAGE);
	private final CheckboxSetting explosionBypass = new CheckboxSetting("Explosion Bypass", "Bypass explosions. => No knockback\n\nBut no particle and no sound.", true);
	
	private final Random random = new Random();
	
	public AntiKnockbackHack()
	{
		super("AntiKnockback");
		setCategory(Category.COMBAT);
		addSetting(hStrength);
		addSetting(vStrength);
		addSetting(activationChance);
		addSetting(explosionBypass);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(KnockbackListener.class, this);
		EVENTS.add(PacketInputListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(KnockbackListener.class, this);
		EVENTS.remove(PacketInputListener.class, this);
	}
	
	@Override
	public void onKnockback(KnockbackEvent event)
	{
		double verticalMultiplier = 1 - vStrength.getValue();
		double horizontalMultiplier = 1 - hStrength.getValue();
		
		if (!(random.nextDouble() <= activationChance.getValue()))
			return;
		
		event.setX(event.getDefaultX() * horizontalMultiplier);
		event.setY(event.getDefaultY() * verticalMultiplier);
		event.setZ(event.getDefaultZ() * horizontalMultiplier);
	}

	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{		
		if (event.getPacket() instanceof ExplosionS2CPacket && explosionBypass.isChecked())
			event.cancel();
	}
}
