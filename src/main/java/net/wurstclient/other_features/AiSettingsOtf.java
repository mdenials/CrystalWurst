package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"Ai Settings", "ai settings"})
@DontBlock
public final class AiSettingsOtf extends OtherFeature
{
public final SliderSetting range = new SliderSetting("Search Range","Max search range", 100, 0, 512, 1, ValueDisplay.INTEGER);
public final SliderSetting thinkSpeed = new SliderSetting("ThinkSpeed","ThinkSpeed value.", 100, 0, 2147483647, 1, ValueDisplay.INTEGER);
public final SliderSetting thinkTime = new SliderSetting("ThinkTime","ThinkTime value.", 100, 0, 2147483647, 1, ValueDisplay.INTEGER);
public final SliderSetting renderedThings = new SliderSetting("RenderedThings","RenderedThings max value.", 100, 0, 5000, 1, ValueDisplay.INTEGER);
public final SliderSetting standartCost = new SliderSetting("Standart Cost","ThinkTime value.", 0.5, 0, 2000, 1, ValueDisplay.DECIMAL);
public final SliderSetting modifierCost = new SliderSetting("Cost Modifier","Cost modifier value.", 0.5, 0, 2000, 1, ValueDisplay.DECIMAL);
public final SliderSetting walkingCost = new SliderSetting("Cost Walking","Cost walking value.", 0.5, 0, 2000, 1, ValueDisplay.DECIMAL);
public final SliderSetting liquidsCost = new SliderSetting("Cost Liquids","Cost liquids value.", 0.5, 0, 2000, 1, ValueDisplay.DECIMAL);
public final SliderSetting slownessCost = new SliderSetting("Cost Slowness","Cost slowness value.", 0.5, 0, 2000, 1, ValueDisplay.DECIMAL);
public final SliderSetting miningCost = new SliderSetting("Cost Mining","Cost mining value.", 0.5, 0, 2000, 1, ValueDisplay.DECIMAL);
public final SliderSetting jumpingCost = new SliderSetting("Cost Jumping","Cost jumping value.", 0.5, 0, 2000, 1, ValueDisplay.DECIMAL);

public AiSettingsOtf()
{
super("AI Settings", "Shows a list of AI parameters on");
addSetting(range);
addSetting(thinkSpeed);
addSetting(thinkTime);
addSetting(renderedThings);
addSetting(standartCost);
addSetting(modifierCost);
addSetting(walkingCost);
addSetting(liquidsCost);
addSetting(slownessCost);
addSetting(miningCost);
addSetting(jumpingCost);
}
}
