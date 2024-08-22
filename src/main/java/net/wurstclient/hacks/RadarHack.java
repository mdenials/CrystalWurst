/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.clickgui.Window;
import net.wurstclient.clickgui.components.RadarComponent;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filters.*;
import net.wurstclient.util.FakePlayerEntity;

@SearchTags({"MiniMap", "mini map"})
public final class RadarHack extends Hack implements UpdateListener
{
	private final SliderSetting radius = new SliderSetting("Radius", "Radius in blocks.", 100, 0, 512, 1, ValueDisplay.INTEGER);
	private final SliderSetting width = new SliderSetting("Width", "Width value.", 96, 1, 2147483647, 1, ValueDisplay.INTEGER);
	private final SliderSetting height = new SliderSetting("Height", "Height value.", 96, 1, 2147483647, 1, ValueDisplay.INTEGER);
	private final CheckboxSetting rotate = new CheckboxSetting("Rotate with player", true);
	private final ColorSetting livingColor = new ColorSetting("Living Color", "Living entities will be highlighted in this color.", Color.RED);
	private final ColorSetting otherColor = new ColorSetting("Other Color", "Other entities will be highlighted in this color.", Color.GREEN);

	private final Window window;
	private final EntityFilterList entityFilters = EntityFilterList.genericVision();
	private final ArrayList<Entity> entities = new ArrayList<>();
	
	public RadarHack()
	{
		super("Radar");
		
		setCategory(Category.RENDER);
		addSetting(radius);
		addSetting(width)
		addSetting(height);
		addSetting(rotate);
		addSetting(livingColor);
		addSetting(otherColor);
		entityFilters.forEach(this::addSetting);
		
		window = new Window("Radar");
		window.setPinned(true);
		window.setInvisible(true);
		window.add(new RadarComponent(this));
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		window.setInvisible(false);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		window.setInvisible(true);
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		ClientWorld world = MC.world;
		
		entities.clear();
		Stream<Entity> stream =
			StreamSupport.stream(world.getEntities().spliterator(), true)
				.filter(e -> !e.isRemoved() && e != player)
				.filter(e -> !(e instanceof FakePlayerEntity))
				.filter(Entity.class::isInstance);
		
		stream = entityFilters.applyTo(stream);
		
		entities.addAll(stream.collect(Collectors.toList()));
	}
	
	public Window getWindow()
	{
		return window;
	}
	
	public Iterable<Entity> getEntities()
	{
		return Collections.unmodifiableList(entities);
	}

	public double getRadius()
	{
		return radius.getValue();
	}
	
	public int getWidth()
	{
		return width.getValue();
	}

	public int getHeight()
	{
		return height.getValue();
	}

	public int getLiving()
	{
		return livingColor.getColorI();
	}

	public int getOther()
	{
		return otherColor.getColorI();
	}
	
	public boolean isRotateEnabled()
	{
		return rotate.isChecked();
	}
}
