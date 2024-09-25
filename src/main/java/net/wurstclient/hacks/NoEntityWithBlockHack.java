/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags({"entities with block"})
public final class NoEntityWithBlockHack extends Hack
{
    	public final CheckboxSetting noMobInSpawner = new CheckboxSetting("MobInSpawner", false);
    	public final CheckboxSetting noEnchTableBook = new CheckboxSetting("EnchantTableBook", false);

    	public NoEntityWithBlockHack()
	{
		super("NoEntityWithBlock");
		setCategory(Category.RENDER);
        	addSetting(noMobInSpawner);
        	addSetting(noEnchTableBook);
    	}
}
