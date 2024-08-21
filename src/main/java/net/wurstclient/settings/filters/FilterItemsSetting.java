package net.wurstclient.settings.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;

public final class FilterItemsSetting extends EntityFilterCheckbox
{
	public FilterItemsSetting(String description, boolean checked)
	{
		super("Filter items", description, checked);
	}
	
	@Override
	public boolean test(Entity e)
	{
		return !(e instanceof ItemEntity);
	}
	
	public static FilterItemsSetting genericCombat(boolean checked)
	{
		return new FilterItemsSetting("Won't attack other items.", checked);
	}
	
	public static FilterItemsSetting genericVision(boolean checked)
	{
		return new FilterItemsSetting("Won't show other items.", checked);
	}
}
