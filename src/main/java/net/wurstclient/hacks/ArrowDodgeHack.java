package net.wurstclient.hacks;

import java.util.Random;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import java.util.function.ToDoubleFunction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.wurstclient.util.EntityUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.RotationUtils;

public final class ArrowDodgeHack extends Hack implements UpdateListener
{
private final EnumSetting<Mode> mode = new EnumSetting<>("Mode", "Set the velocity mode", Mode.values(), Mode.VELOCITY);
private final EnumSetting<Priority> priority = new EnumSetting<>("Priority", Priority.values(), Priority.ANGLE);
private final SliderSetting range = new SliderSetting("Range", 5, 0, 20, 0.000001, ValueDisplay.DECIMAL);
private final SliderSetting fov = new SliderSetting("FOV", 360, 1, 360, 1, ValueDisplay.DEGREES);
private final SliderSetting max = new SliderSetting("Max Random", 0, -20, 20, 0.000001, ValueDisplay.DECIMAL);
private final SliderSetting min = new SliderSetting("Min Random", 0, -20, 20, 0.000001, ValueDisplay.DECIMAL);

private Entity entity;


    public ArrowDodgeHack()
	{
		super("ArrowDodge");
		setCategory(Category.MOVEMENT);
        addSetting(mode);
        addSetting(priority);
        addSetting(range);
        addSetting(fov);
        addSetting(max);
        addSetting(min);
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
    Stream<Entity> stream = EntityUtils.getEntities();
    ClientPlayerEntity player = MC.player;
    Vec3d v = player.getVelocity();
    double rangeSq = Math.pow(range.getValue(), 2);
    stream = stream.filter(e -> MC.player.squaredDistanceTo(e) <= rangeSq);

    if(fov.getValue() < 360.0) 
    {
        stream = stream.filter(e -> RotationUtils.getAngleToLookVec(e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);
    }

    stream = stream.filter(e -> e instanceof ArrowEntity);

    entity = stream.min(priority.getSelected().comparator).orElse(null);
    if(entity == null) return;

    Random random = new Random();
    double rX = min.getValue() + (max.getValue() - min.getValue()) * random.nextDouble();
    double rZ = min.getValue() + (max.getValue() - min.getValue()) * random.nextDouble();

    if (mode.getSelected() == Mode.VELOCITY) player.setVelocity(v.x + rX, v.y, v.z + rZ);
    if (mode.getSelected() == Mode.TELEPORT) player.setPosition(entity.getX() + rX, entity.getY(), entity.getZ() + rZ);
    }


    private enum Priority
	{
		DISTANCE("Distance", e -> MC.player.squaredDistanceTo(e)),
		ANGLE("Angle", e -> RotationUtils.getAngleToLookVec(e.getBoundingBox().getCenter()));
		
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

    private enum Mode
	{
        VELOCITY("Velocity"),
        TELEPORT("Teleport");

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
