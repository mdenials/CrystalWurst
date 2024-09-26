/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import java.awt.Color;
import java.util.function.BooleanSupplier;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.TextFieldSetting;

@SearchTags({"wurst logo", "top left corner"})
@DontBlock
public final class WurstLogoOtf extends OtherFeature
{
	private final TextFieldSetting versionString = new TextFieldSetting("VersionString","WurstClient_v8.0.0_MC1.20.6");
	
	private final ColorSetting bgColor = new ColorSetting("Background",
		"Background color.\n"
			+ "Only visible when \u00a76RainbowUI\u00a7r is disabled.",
		Color.WHITE);
	
	private final ColorSetting txtColor =
		new ColorSetting("Text", "Text color.", Color.BLACK);
	
	private final EnumSetting<Visibility> visibility =
		new EnumSetting<>("Visibility", Visibility.values(), Visibility.ON);
	
	public WurstLogoOtf()
	{
		super("WurstLogo", "Shows the Wurst logo and version on the screen.");
		addSetting(versionString);
		addSetting(bgColor);
		addSetting(txtColor);
		addSetting(visibility);
	}

	public boolean isVisible()
	{
		return visibility.getSelected().isVisible();
	}

	public String getVersion()
	{
		return this.versionString.getValue();
	}
	
	public int getTextColor()
	{
		return txtColor.getColorI();
	}

	public int getBackgroundColor()
	{
		return bgColor.getColorI();
	}
	
	public static enum Visibility
	{
		ON("On", () -> true),
		OFF("Off", () -> false);
		
		private final String name;
		private final BooleanSupplier visible;
		
		private Visibility(String name, BooleanSupplier visible)
		{
			this.name = name;
			this.visible = visible;
		}
		
		public boolean isVisible()
		{
			return visible.getAsBoolean();
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
