/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

import java.awt.Color;

import net.minecraft.util.math.MathHelper;
import net.wurstclient.util.json.JsonException;

public enum ColorUtils
{
	;
	
	public static String toHex(Color color)
	{
		return String.format("#%08X", color.getRGB());
	}
	
	public static Color parseHex(String s) throws JsonException
	{
		if(!s.startsWith("#"))
			throw new JsonException("Missing '#' prefix.");
		
		if(s.length() != 9)
			throw new JsonException(
				"Expected String of length 7, got " + s.length() + " instead.");
		
		int[] rgb = new int[4];
		
		try
		{
			for(int i = 0; i < 4; i++)
			{
				String channelString = s.substring(i * 2, i * 2 + 2);
				int channel = Integer.parseUnsignedInt(channelString, 16);
				rgb[i] = MathHelper.clamp(channel, 0, 255);
			}
			
		}catch(NumberFormatException e)
		{
			throw new JsonException("ColorUtils.ParseHex(Error)r");
		}
		
		return new Color(rgb[0], rgb[1], rgb[2], rgb[3]);
	}
	
	public static Color tryParseHex(String s)
	{
		try
		{
			return parseHex(s);
			
		}catch(JsonException e)
		{
			return null;
		}
	}
	
	public static Color parseRGB(String red, String green, String blue, String alpha)
		throws JsonException
	{
		String[] rgbStrings = {red, green, blue, alpha};
		int[] rgb = new int[4];
		
		try
		{
			for(int i = 0; i < 4; i++)
			{
				int channel = Integer.parseInt(rgbStrings[i]);
				rgb[i] = MathHelper.clamp(channel, 0, 255);
			}
			
		}catch(NumberFormatException e)
		{
			throw new JsonException("ColorUtils.ParseRGB(Error)");
		}
		
		return new Color(rgb[0], rgb[1], rgb[2], rgb[3]);
	}
	
	public static Color tryParseRGB(String red, String green, String blue, String alpha)
	{
		try
		{
			return parseRGB(red, green, blue, alpha);
			
		}
		catch(JsonException e)
		{
			return null;
		}
	}
}
