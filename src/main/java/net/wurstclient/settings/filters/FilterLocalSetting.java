package net.wurstclient.settings.filters;

import net.minecraft.entity.Entity;
import net.minecraft.client.network.ClientPlayerEntity;

public final class FilterLocalSetting extends EntityFilterCheckbox
{
	public FilterLocalSetting(String description, boolean checked)
	{
		super("Filter local", description, checked);
	}
	
	@Override
	public boolean test(Entity e)
	{
		return !(e instanceof ClientPlayerEntity);
	}
	
	public static FilterLocalSetting genericCombat(boolean checked)
	{
		return new FilterLocalSetting("Won't attack local.", checked);
	}
	
	public static FilterLocalSetting genericVision(boolean checked)
	{
		return new FilterLocalSetting("Won't show local.", checked);
	}
}
