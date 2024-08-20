/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings.filterlists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.entity.Entity;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.filters.*;

public class EntityFilterList
{
	private final List<EntityFilter> entityFilters;
	
	public EntityFilterList(EntityFilter... filters)
	{
		this(Arrays.asList(filters));
	}
	
	public EntityFilterList(List<EntityFilter> filters)
	{
		entityFilters = Collections.unmodifiableList(filters);
	}
	
	public final void forEach(Consumer<? super Setting> action)
	{
		entityFilters.stream().map(EntityFilter::getSetting).forEach(action);
	}
	
	public final <T extends Entity> Stream<T> applyTo(Stream<T> stream)
	{
		for(EntityFilter filter : entityFilters)
		{
			if(!filter.isFilterEnabled())
				continue;
			
			stream = stream.filter(filter);
		}
		
		return stream;
	}
	
	public final boolean testOne(Entity entity)
	{
		for(EntityFilter filter : entityFilters)
			if(filter.isFilterEnabled() && !filter.test(entity))
				return false;
			
		return true;
	}
	
	public static EntityFilterList genericCombat()
	{
		return new EntityFilterList(FilterPlayersSetting.genericCombat(false),
			FilterSleepingSetting.genericCombat(false),
			FilterFlyingSetting.genericCombat(0),
			FilterHostileSetting.genericCombat(false),
			FilterNeutralSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterPassiveSetting.genericCombat(false),
			FilterPassiveWaterSetting.genericCombat(false),
			FilterBabiesSetting.genericCombat(false),
			FilterBatsSetting.genericCombat(false),
			FilterSlimesSetting.genericCombat(false),
			FilterPetsSetting.genericCombat(false),
			FilterVillagersSetting.genericCombat(false),
			FilterZombieVillagersSetting.genericCombat(false),
			FilterGolemsSetting.genericCombat(false),
			FilterPiglinsSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterZombiePiglinsSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterEndermenSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterShulkersSetting.genericCombat(false),
			FilterAllaysSetting.genericCombat(false),
			FilterInvisibleSetting.genericCombat(false),
			FilterLocalSetting.genericCombat(false),
			FilterNamedSetting.genericCombat(false),
			FilterShulkerBulletSetting.genericCombat(false),
			FilterFireballSetting.genericCombat(false),
			FilterArmorStandsSetting.genericCombat(false),
			FilterCrystalsSetting.genericCombat(false));
	}
	public static EntityFilterList genericVision()
	{
		return new EntityFilterList(FilterPlayersSetting.genericVision(false),
			FilterSleepingSetting.genericVision(false),
			FilterHostileSetting.genericVision(false),
			FilterNeutralSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF),
			FilterPassiveSetting.genericVision(false),
			FilterPassiveWaterSetting.genericVision(false),
			FilterBabiesSetting.genericVision(false),
			FilterBatsSetting.genericVision(false),
			FilterSlimesSetting.genericVision(false),
			FilterPetsSetting.genericVision(false),
			FilterVillagersSetting.genericVision(false),
			FilterZombieVillagersSetting.genericVision(false),
			FilterGolemsSetting.genericVision(false),
			FilterPiglinsSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF),
			FilterZombiePiglinsSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF),
			FilterEndermenSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF),
			FilterShulkersSetting.genericVision(false),
			FilterAllaysSetting.genericVision(false),
			FilterInvisibleSetting.genericVision(false),
			FilterLocalSetting.genericCombat(false),
			FilterNamedSetting.genericVision(false),
			FilterShulkerBulletSetting.genericVision(false),
			FilterFireballSetting.genericVision(false),
			FilterArmorStandsSetting.genericVision(false),
			FilterCrystalsSetting.genericVision(false));
	}
	
	public static interface EntityFilter extends Predicate<Entity>
	{
		public boolean isFilterEnabled();
		
		public Setting getSetting();
	}
}
