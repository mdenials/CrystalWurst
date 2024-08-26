/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Random;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.Category;
import net.wurstclient.hack.Hack;
import net.wurstclient.SearchTags;

public final class SuperKnockbackHack extends Hack implements LeftClickListener
{
    	private final EnumSetting<Mode> mode = new EnumSetting<>("Mode",
		"\u00a7lPacket\u00a7r mode sends packets to server without actually moving you at all.\n\n"
			+ "\u00a7lSprint Tap\u00a7r mode press a sprint that is just enough to get a extended knockback.\n\n",
		Mode.values(), Mode.PACKET);

    	private final SliderSetting activationChance = new SliderSetting("Activation chance",
		"Only activate superknockback with the given chance",
		1, 0, 1, 0.000001, ValueDisplay.PERCENTAGE);

    	private final SliderSetting delay = new SliderSetting("Delay", 0, 0, 20, 1, ValueDisplay.INTEGER);

    	private final Random random = new Random();
    	private boolean chance;
    	private int timer;

    	public SuperKnockbackHack()
	{
		super("SuperKnockback");
		setCategory(Category.COMBAT);
        	addSetting(mode);
		addSetting(activationChance);
        	addSetting(delay);
	}


    	@Override
	protected void onEnable()
	{
        	timer = 0;
		EVENTS.add(LeftClickListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(LeftClickListener.class, this);
	}

	@Override
	public void onLeftClick(LeftClickEvent event)
	{
		if(MC.crosshairTarget == null
			|| MC.crosshairTarget.getType() != HitResult.Type.ENTITY
			|| !(((EntityHitResult)MC.crosshairTarget).getEntity() instanceof LivingEntity))
			return;


        	// wait for timer
		if(timer > 0)
		{
			timer--;
			return;
		}
        	timer = delay.getValueI();

        	chance = random.nextDouble() <= activationChance.getValue();
        	if (chance) doKnockback();			
	}

    	public void doKnockback()
	{
		if(!isEnabled())
			return;

		switch(mode.getSelected())
		{
			case PACKET:
			doPacket();
			break;
			
			case SPRINTTAP:
			doSprintTap();
			break;
		}
	}

    	private void doPacket()
	{
        	MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
	}

	private void doSprintTap()
	{   
        	MC.player.setSprinting(true);
	}

    	private enum Mode
	{
		PACKET("Packet"),
		SPRINTTAP("Sprint");
		
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
