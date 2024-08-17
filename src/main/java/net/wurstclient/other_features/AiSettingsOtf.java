package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;
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
public final CheckboxSetting mineable = new CheckboxSetting("Mineable", false);
public final CheckboxSetting extraMoves = new CheckboxSetting("ExtraMoves", false);
public final CheckboxSetting extraDiagonalMoves = new CheckboxSetting("ExtraDiagonalMoves", false);
public final CheckboxSetting falling = new CheckboxSetting("Allow falling", true);
public final CheckboxSetting diving = new CheckboxSetting("Allow diving", true);
public final CheckboxSetting flying = new CheckboxSetting("Allow flying", true);



public AiSettingsOtf()
{
super("AI Settings", "Shows a list of AI parameters on");
addSetting(range);
addSetting(thinkSpeed);
addSetting(thinkTime);
addSetting(renderedThings);
addSetting(mineable);
addSetting(extraMoves);
addSetting(extraDiagonalMoves);
addSetting(falling);
addSetting(diving);
addSetting(flying);
}
}
