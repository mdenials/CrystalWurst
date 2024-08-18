package net.wurstclient.hacks;


import net.minecraft.entity.Entity;
import net.wurstclient.Category;
import net.wurstclient.hack.Hack;
import net.wurstclient.SearchTags;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.filterlists.EntityFilterList;

@SearchTags({"hitbox", "hitboxes", "boxes"})
public final class HitboxHack extends Hack
{
  public final SliderSetting sv = new SliderSetting("XZ Value", 1, 0, 20, 0.000001, ValueDisplay.DECIMAL);
  public final SliderSetting hv = new SliderSetting("Y Value", 1, 0, 20, 0.000001, ValueDisplay.DECIMAL);
	
  private final EntityFilterList entityFilters = EntityFilterList.genericCombat();

	public HitboxHack()
	{
		super("Hitbox");
		setCategory(Category.MOVEMENT);
		addSetting(sv);
        addSetting(hv);

        entityFilters.forEach(this::addSetting);
	}

   public boolean shouldBeExpand(Entity entity)
	{
	return isEnabled() && entityFilters.testOne(entity);
	}
}
