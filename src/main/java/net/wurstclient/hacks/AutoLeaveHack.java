/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.TextFieldSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.InventoryUtils;

@SearchTags({"auto leave", "AutoDisconnect", "auto disconnect", "AutoQuit",
	"auto quit"})
public final class AutoLeaveHack extends Hack implements UpdateListener
{
	private final SliderSetting health = new SliderSetting("Health",
		"Leaves the server when your health reaches this value or falls below it.",
		4, 0, 20, 0.000001, ValueDisplay.DECIMAL.withSuffix(" hearts"));
	
	public final EnumSetting<Mode> mode = new EnumSetting<>("Mode",
		"\u00a7lQuit\u00a7r mode just quits the game normally.\n"
			+ "Bypasses NoCheat+ but not CombatLog.\n\n"
			+ "\u00a7lChars\u00a7r mode sends a special chat message that"
			+ " causes the server to kick you.\n"
			+ "Bypasses NoCheat+ and some versions of CombatLog.\n\n"
			+ "\u00a7lSelfHurt\u00a7r mode sends the packet for attacking"
			+ " another player, but with yourself as both the attacker and the"
			+ " target, causing the server to kick you.\n"
			+ "Bypasses both CombatLog and NoCheat+.",
		Mode.values(), Mode.QUIT);

	private final TextFieldSetting invalidChar = new TextFieldSetting("InvalidCharField", "\u00a7");
	
	private final CheckboxSetting disableAutoReconnect = new CheckboxSetting(
		"Disable AutoReconnect", "Automatically turns off AutoReconnect when"
			+ " AutoLeave makes you leave the server.",
		true);
	
	private final SliderSetting totems = new SliderSetting("Totems",
		"Won't leave the server until the number of totems you have reaches"
			+ " this value or falls below it.\n\n"
			+ "11 = always able to leave",
		11, 0, 36, 1, ValueDisplay.INTEGER.withSuffix(" totems"));
	
	public AutoLeaveHack()
	{
		super("AutoLeave");
		setCategory(Category.COMBAT);
		addSetting(health);
		addSetting(mode);
		addSetting(invalidChar);
		addSetting(disableAutoReconnect);
		addSetting(totems);
	}
	
	@Override
	public String getRenderName()
	{
		if(MC.player.getAbilities().creativeMode)
			return getName() + " (paused)";
		
		return getName() + " [" + mode.getSelected() + "]";
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
		// check health
		float currentHealth = MC.player.getHealth();
		if(currentHealth <= 0F || currentHealth > health.getValueF())
			return;
		
		// check totems
		if(InventoryUtils.count(Items.TOTEM_OF_UNDYING, 40, true) > totems.getValueI())
			return;
		
		// leave server
		mode.getSelected().leave.run();
		
		if(disableAutoReconnect.isChecked())
			WURST.getHax().autoReconnectHack.setEnabled(false);
	}
	
	public static enum Mode
	{
		QUIT("Quit", () -> MC.world.disconnect()),
		CHARS("Chars", () -> MC.getNetworkHandler().sendChatMessage(invalidChar.getValue())),
		SELFHURT("SelfHurt", () -> MC.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(MC.player, MC.player.isSneaking())));
		
		private final String name;
		private final Runnable leave;
		public static char = invalidChar.getValue()
		
		private Mode(String name, Runnable leave)
		{
			this.name = name;
			this.leave = leave;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
