package net.wurstclient.hacks;

import net.minecraft.client.util.math.MatrixStack;
import net.wurstclient.Category;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.events.UpdateListener;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Items;

public final class BowSpamHack extends Hack implements UpdateListener
{
    private final SliderSetting charge = new SliderSetting("Charge",
    "How long to charge the bow before releasing in ticks.",
		5, 0, 20, 1, ValueDisplay.INTEGER);

    private final CheckboxSetting holdingRightClick = new CheckboxSetting(
		"When holding right click",
		"Works only when holding right click.",
		true);

    private boolean wasBow = false;
    private boolean wasHoldingRightClick = false;

    public BowSpamHack()
	{
		super("BowSpam");
		setCategory(Category.COMBAT);
        addSetting(charge);
        addSetting(holdingRightClick);
	}


    @Override
	protected void onEnable()
	{
        wasBow = false;
        wasHoldingRightClick = false;
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
        setPressed(false);
		EVENTS.remove(UpdateListener.class, this);
	}

    @Override
	public void onUpdate() 
    {

    if (!holdingRightClick.isChecked() || MC.options.useKey.isPressed())
    {
    boolean isBow = MC.player.getMainHandStack().getItem() == Items.BOW;
    if (!isBow && wasBow) setPressed(false);

    wasBow = isBow;
    if (!isBow) return;

    if (MC.player.getItemUseTime() >= charge.getValueI())
    {
        MC.player.stopUsingItem();
        MC.interactionManager.stopUsingItem(MC.player);
    } 
    else 
    {
        setPressed(true);
    }

    wasHoldingRightClick = MC.options.useKey.isPressed();
    }
    else {
    if (wasHoldingRightClick) 
            {
                setPressed(false);
                wasHoldingRightClick = false;
            }
        }
    }

    private void setPressed(boolean pressed)
    {
        MC.options.useKey.setPressed(pressed);
    }
}
