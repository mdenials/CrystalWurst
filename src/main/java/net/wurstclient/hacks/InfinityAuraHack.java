/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.function.ToDoubleFunction;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.wurstclient.Category;
import net.wurstclient.hack.Hack;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.FacingSetting;
import net.wurstclient.settings.FacingSetting.Facing;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.SwingHandSetting.SwingHand;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.PauseAttackOnContainersSetting;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.Rotation;
import net.wurstclient.util.RotationUtils;

@SearchTags({"infinity aura", "long aura"})
public final class InfinityAuraHack extends Hack implements UpdateListener
{

	private final EnumSetting<Mode> mode = new EnumSetting<>("Mode", "\u00a7lSingle\u00a7r aura can damage one entity.\n" + "\u00a7lMulti\u00a7r aura mode can damage multiple entities per hit.", Mode.values(), Mode.SINGLE);

	private final SliderSetting range = new SliderSetting("Range", 1, 0, 512, 0.000001, ValueDisplay.DECIMAL.withLabel(0, "infinity"));

	private final EnumSetting<Priority> priority = new EnumSetting<>("Priority",
		"Determines which entity will be attacked first.\n"
			+ "\u00a7lDistance\u00a7r - Attacks the closest entity.\n"
			+ "\u00a7lAngle\u00a7r - Attacks the entity that requires the least head movement.\n"
			+ "\u00a7lHealth\u00a7r - Attacks the weakest entity.",
		Priority.values(), Priority.ANGLE);

    	private final AttackSpeedSliderSetting speed = new AttackSpeedSliderSetting();
	
    	private final FacingSetting facing = FacingSetting.withPacketSpam("Face entities",
			"Whether or not InfinityAura should face the correct direction when rotate to entites.\n\n"
				+ "Slower but can help with anti-cheat plugins.",
			Facing.OFF);
	
	private final SwingHandSetting swingHand =
		new SwingHandSetting(this, SwingHand.CLIENT);
	
    	private final SliderSetting fov = new SliderSetting("FOV", 360, 1, 360, 1, ValueDisplay.DEGREES);
	private final PauseAttackOnContainersSetting pauseOnContainers = new PauseAttackOnContainersSetting(false);
    	private final CheckboxSetting checkLOS = new CheckboxSetting("Check line of sight", false);

    	private double iX,iY,iZ;
    	private Entity target;
	private final EntityFilterList entityFilters = EntityFilterList.genericCombat();
	
	public InfinityAuraHack()
	{
		super("InfinityAura");
		setCategory(Category.COMBAT);
		addSetting(mode);
		addSetting(range);
        	addSetting(priority);
		addSetting(speed);
        	addSetting(facing);
        	addSetting(swingHand);
        	addSetting(fov);
		addSetting(pauseOnContainers);
        	addSetting(checkLOS);
		
		entityFilters.forEach(this::addSetting);
	}
	
	@Override
	protected void onEnable()
	{
		// disable other killauras
		WURST.getHax().aimAssistHack.setEnabled(false);
		WURST.getHax().clickAuraHack.setEnabled(false);
		WURST.getHax().crystalAuraHack.setEnabled(false);
		WURST.getHax().fightBotHack.setEnabled(false);
		WURST.getHax().killauraHack.setEnabled(false);
		WURST.getHax().protectHack.setEnabled(false);
		WURST.getHax().tpAuraHack.setEnabled(false);
		WURST.getHax().triggerBotHack.setEnabled(false);
        	WURST.getHax().multiAuraHack.setEnabled(false);
		
		speed.resetTimer();
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
        	target = null;
	}
	
	@Override
	public void onUpdate()
	{
		speed.updateTimer();
		if(!speed.isTimeToAttack())
			return;
		
		if(pauseOnContainers.shouldPause())
			return;

        	double rangeSq = range.getValue() == 0 ? Double.POSITIVE_INFINITY : range.getValueSq();

		ClientPlayerEntity player = MC.player;
		ClientPlayNetworkHandler netHandler = player.networkHandler;

        	// get entities
		Stream<Entity> s1 = EntityUtils.getAttackableEntities();
        	Stream<Entity> s2 = EntityUtils.getAttackableEntities();

		s1 = s1.filter(e -> MC.player.squaredDistanceTo(e) <= rangeSq);
        	s2 = s2.filter(e -> MC.player.squaredDistanceTo(e) <= rangeSq);

        	if(fov.getValue() < 360.0) 
        	{
        		s1 = s1.filter(e -> RotationUtils.getAngleToLookVec(e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);
        		s2 = s2.filter(e -> RotationUtils.getAngleToLookVec(e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);
        	}

		s1 = entityFilters.applyTo(s1);
        	s2 = entityFilters.applyTo(s2);

        	target = s2.min(priority.getSelected().comparator).orElse(null);

        	if(target == null) return;

		ArrayList<Entity> entities = s1.collect(Collectors.toCollection(ArrayList::new));

        	Vec3d hitVec = target.getBoundingBox().getCenter();
        	if(checkLOS.isChecked() && !BlockUtils.hasLineOfSight(hitVec))
		{
			target = null;
			return;
		}

		if(entities.isEmpty()) return;

        	if(mode.getSelected() == Mode.SINGLE)
        	{
            		netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(target.getX(), target.getY(), target.getZ(), target.isOnGround()));
            		facing.getSelected().face(hitVec);
            		MC.interactionManager.attackEntity(player, target);        
        	}

        	if(mode.getSelected() == Mode.MULTI)
        	{
         		// attack entities
			for(Entity entity : entities)
        		{
				Rotation rotations = RotationUtils.getNeededRotations(entity.getBoundingBox().getCenter());
                		netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(entity.getX(), entity.getY(), entity.getZ(), entity.isOnGround()));
				WurstClient.MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotations.yaw(), rotations.pitch(), MC.player.isOnGround()));
				MC.interactionManager.attackEntity(player, entity);
			}
        	}

        	swingHand.getSelected().swing(Hand.MAIN_HAND);
		speed.resetTimer();
	}

    	private enum Mode
	{
		SINGLE("Single"),
		MULTI("Multi");
		
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

    	private enum Priority
	{
		DISTANCE("Distance", e -> MC.player.squaredDistanceTo(e)),
		ANGLE("Angle", e -> RotationUtils.getAngleToLookVec(e.getBoundingBox().getCenter())),
		HEALTH("Health", e -> e instanceof LivingEntity ? ((LivingEntity)e).getHealth() : Integer.MAX_VALUE);
		
		private final String name;
		private final Comparator<Entity> comparator;
		
		private Priority(String name, ToDoubleFunction<Entity> keyExtractor)
		{
			this.name = name;
			comparator = Comparator.comparingDouble(keyExtractor);
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
