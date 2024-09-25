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

@SearchTags({"map overlay"})
public final class NoMapOverlayHack extends Hack
{

    private final CheckboxSetting noMapMarkers = new CheckboxSetting("MapMarkers", false);
    private final CheckboxSetting noMapContents = new CheckboxSetting("MapContents", false);

    public NoMapOverlayHack()
	{
		super("NoMapOverlay");
		setCategory(Category.RENDER);
        addSetting(noMapMarkers);
        addSetting(noMapContents)
    }

}
