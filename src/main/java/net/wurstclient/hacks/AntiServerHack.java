/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags({"NoRender","render","particle","bypass","RenderBypass","ParticleBypass"})
public final class AntiServerHack extends Hack implements PacketInputListener
{
        private final CheckboxSetting particleS2C = new CheckboxSetting("ParticleS2CPacket", false);
        private final CheckboxSetting playSoundS2C = new CheckboxSetting("PlaySoundS2CPacket", false);
        private final CheckboxSetting playSoundFromEntityS2C = new CheckboxSetting("PlaySoundFromEntityS2CPacket", false);
        private final CheckboxSetting enterCombatS2C = new CheckboxSetting("EnterCombatS2CPacket", false);
        private final CheckboxSetting endCombatS2C = new CheckboxSetting("EndCombatS2CPacket", false);
        private final CheckboxSetting playerAbilitiesS2C = new CheckboxSetting("PlayerAbilitiesS2CPacket", false);
        private final CheckboxSetting openScreenS2C = new CheckboxSetting("OpenScreenS2CPacket", false);
        private final CheckboxSetting closeScreenS2C = new CheckboxSetting("CloseScreenS2CPacket", false);
        private final CheckboxSetting lookAtS2C = new CheckboxSetting("LookAtS2CPacket", false);
        private final CheckboxSetting playerPositionLookS2C = new CheckboxSetting("PlayerPositionLookS2CPacket", false);
        private final CheckboxSetting entityStatusEffectsS2C = new CheckboxSetting("EntityStatusEffectS2CPacket", false);
        private final CheckboxSetting chunkLoadDistanceS2C = new CheckboxSetting("ChunkLoadDistanceS2CPacket", false);
        private final CheckboxSetting bossBarS2C = new CheckboxSetting("BossBarS2CPacket", false);
        private final CheckboxSetting scoreboardDisplayS2C = new CheckboxSetting("ScoreboardDisplayS2CPacket", false);
        private final CheckboxSetting clearTitleS2C = new CheckboxSetting("ClearTitleS2CPacket", false);
        private final CheckboxSetting craftFailedResponseS2C = new CheckboxSetting("CraftFailedResponseS2CPacket", false);

	public AntiServerHack()
	{
		super("AntiServer");
		setCategory(Category.RENDER);
        addSetting(particleS2C);
        addSetting(playSoundS2C);
        addSetting(playSoundFromEntityS2C);
        addSetting(enterCombatS2C);
        addSetting(endCombatS2C);
        addSetting(playerAbilitiesS2C);
        addSetting(openScreenS2C);
        addSetting(closeScreenS2C);
        addSetting(lookAtS2C);
        addSetting(playerPositionLookS2C);
        addSetting(entityStatusEffectsS2C);
        addSetting(chunkLoadDistanceS2C);
        addSetting(bossBarS2C);
        addSetting(scoreboardDisplayS2C);
        addSetting(clearTitleS2C);
        addSetting(craftFailedResponseS2C);       
		
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(PacketInputListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(PacketInputListener.class, this);
	}

	@Override
	public void onReceivedPacket(PacketInputEvent event) {
		if (event.getPacket() instanceof ParticleS2CPacket && particleS2C.isChecked()
            || event.getPacket() instanceof PlaySoundS2CPacket && playSoundS2C.isChecked()
            || event.getPacket() instanceof PlaySoundFromEntityS2CPacket && playSoundFromEntityS2C.isChecked()
            || event.getPacket() instanceof EnterCombatS2CPacket && enterCombatS2C.isChecked()
            || event.getPacket() instanceof EndCombatS2CPacket && endCombatS2C.isChecked()
            || event.getPacket() instanceof PlayerAbilitiesS2CPacket && playerAbilitiesS2C.isChecked()
            || event.getPacket() instanceof OpenScreenS2CPacket && openScreenS2C.isChecked()
            || event.getPacket() instanceof CloseScreenS2CPacket && closeScreenS2C.isChecked()
            || event.getPacket() instanceof LookAtS2CPacket && lookAtS2C.isChecked()
            || event.getPacket() instanceof PlayerPositionLookS2CPacket && playerPositionLookS2C.isChecked()
            || event.getPacket() instanceof EntityStatusEffectS2CPacket && entityStatusEffectsS2C.isChecked()
            || event.getPacket() instanceof ChunkLoadDistanceS2CPacket && chunkLoadDistanceS2C.isChecked()
            || event.getPacket() instanceof BossBarS2CPacket && bossBarS2C.isChecked()
            || event.getPacket() instanceof ScoreboardDisplayS2CPacket && scoreboardDisplayS2C.isChecked()
            || event.getPacket() instanceof ClearTitleS2CPacket && clearTitleS2C.isChecked()
            || event.getPacket() instanceof CraftFailedResponseS2CPacket && craftFailedResponseS2C.isChecked())
        {
			event.cancel();
		}
		//ChatUtils.message(event.getPacket().toString());
	}
}
