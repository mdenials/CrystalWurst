package net.wurstclient.hacks;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.settings.CheckboxSetting;

public final class NoPitchLimitHack extends Hack
{
    public NoPitchLimitHack()
	{
		super("NoPitchLimit");
		setCategory(Category.RENDER);
	}
}
