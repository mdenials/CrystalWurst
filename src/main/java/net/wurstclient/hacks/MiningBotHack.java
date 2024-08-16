/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

//import java.util.Objects;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Deque;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.ai.PathFinder;
import net.wurstclient.ai.PathPos;
import net.wurstclient.ai.PathProcessor;
import net.wurstclient.commands.PathCmd;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.miningbot.Mining;
import net.wurstclient.hacks.miningbot.MiningBotUtils;
import net.wurstclient.settings.FacingSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.BlockSetting;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockBreaker.BlockBreakingParams;
import net.wurstclient.util.BlockUtils;

@SearchTags({"mining bot"})
@DontSaveState
public final class MiningBotHack extends Hack implements UpdateListener, RenderListener
{

	private final SliderSetting range = new SliderSetting("Range", "How far MiningBot will reach to break blocks.", 4.5, 0, 6, 0.000001, ValueDisplay.DECIMAL);
    	private final SliderSetting boxRange = new SliderSetting("Box Range", "Box Range", 1, 0, 20, 1, ValueDisplay.INTEGER);
    	private final SliderSetting arraySize = new SliderSetting("Array Size", 1, 0, 8192, 1, ValueDisplay.INTEGER);
    	private final SliderSetting queueSize = new SliderSetting("Queue Size", 1, 0, 8192, 1, ValueDisplay.INTEGER);
    	private final SliderSetting height = new SliderSetting("Height", "Leaves up height", 1, -256, 256, 1, ValueDisplay.INTEGER);
	private final CheckboxSetting checkAngleLOS = new CheckboxSetting("Check Angle line of sight", true);
    	private final CheckboxSetting checkBreakLOS = new CheckboxSetting("Check Break line of sight", true);

    	private final FacingSetting facing = FacingSetting.withoutPacketSpam(
		"How to face the logs and leaves when breaking them.\n\n"
			+ "\u00a7lOff\u00a7r - Don't face the blocks at all. Will be"
			+ " detected by anti-cheat plugins.\n\n"
			+ "\u00a7lServer-side\u00a7r - Face the blocks on the"
			+ " server-side, while still letting you move the camera freely on"
			+ " the client-side.\n\n"
			+ "\u00a7lClient-side\u00a7r - Face the blocks by moving your"
			+ " camera on the client-side. This is the most legit option, but"
			+ " can be disorienting to look at.");
	
	private final SwingHandSetting swingHand = new SwingHandSetting(
		"How MiningBot should swing your hand when breaking logs and leaves.\n\n"
			+ "\u00a7lOff\u00a7r - Don't swing your hand at all. Will be detected"
			+ " by anti-cheat plugins.\n\n"
			+ "\u00a7lServer-side\u00a7r - Swing your hand on the server-side,"
			+ " without playing the animation on the client-side.\n\n"
			+ "\u00a7lClient-side\u00a7r - Swing your hand on the client-side."
			+ " This is the most legit option.");

    public final BlockListSetting oresList = new BlockListSetting("Ores List", "The types of blocks to break", 
    "minecraft:oak_log", 
    "minecraft:birch_log", 
    "minecraft:spruce_log", 
    "minecraft:mangrove_log",
    "minecraft:jungle_log",
    "minecraft:acacia_log",
    "minecraft:dark_oak_log",
    "minecraft:cherry_log");

    public final BlockListSetting filterList = new BlockListSetting("Filter List", "The types of blocks to be filtered", "minecraft:air");

	private MiningFinder miningFinder;
	private AngleFinder angleFinder;
	private MiningBotPathProcessor processor;
    	private MiningBotPathFinder pathFinder;
	private Mining mining;
	private BlockPos currentBlock;

	public MiningBotHack()
	{
		super("MiningBot");
		setCategory(Category.BLOCKS);
		addSetting(range);
        	addSetting(boxRange);
        	addSetting(arraySize);
        	addSetting(queueSize);
        	addSetting(height);
        	addSetting(checkAngleLOS);
        	addSetting(checkBreakLOS);
		addSetting(facing);
		addSetting(swingHand);
        	addSetting(oresList);
        	addSetting(filterList);
	}
	
	@Override
	public String getRenderName()
	{
        if(miningFinder != null && !miningFinder.isDone() && !miningFinder.isFailed())
			return getName() + " [Searching]";
		
		if(processor != null && !processor.isDone())
			return getName() + " [Going]";
		
		if(mining != null && !mining.getLogs().isEmpty())
			return getName() + " [Mining]";

		//if(mining != null && !mining.getLogs().isEmpty()) return getName() + " ["+ mining.getLogs().size() +"]";
        return getName();
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
        	miningFinder = new MiningFinder();
	}
	
	@Override
	protected void onDisable()
	{
        	processor = null;
        	angleFinder = null;
		miningFinder = null;
        	PathProcessor.releaseControls();

		if(mining != null)
		{
            		mining.close();
            		mining = null;		
		}
		
		if(currentBlock != null)
		{
            		MC.interactionManager.cancelBlockBreaking();
            		MC.interactionManager.breakingBlock = true;
            		currentBlock = null;		
		}
        	EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
	}	

	@Override
	public void onUpdate()
	{
        	if(miningFinder != null)
		{
			goToMining();
			return;
		}

        	if(mining == null)
		{
			miningFinder = new MiningFinder();
            		return;
		}

        	mining.getLogs().removeIf(Predicate.not(MiningBotUtils::isLog));
        	mining.compileBuffer();

        	if(mining.getLogs().isEmpty())
		{
            		mining.close();
            		mining = null;
			return;
		}

        	if(angleFinder != null)
		{
			goToAngle();
			return;
		}

        if(breakBlocks(mining.getLogs())) return; 

        if(angleFinder == null)
        {
            angleFinder = new AngleFinder();
        }   
	}

	private void goToMining()
	{
        	// find path
		if(!miningFinder.isDoneOrFailed())
		{
			PathProcessor.lockControls();
            		miningFinder.findPath();
			return;
		}

        	// process path
		if(processor != null && !processor.isDone())
		{
			processor.goToGoal();
			return;
		}

        miningFinder = null;
        PathProcessor.releaseControls();    	
	}
	
	private void goToAngle()
	{
        	// find path
		if(!angleFinder.isDone() && !angleFinder.isFailed())
		{
			PathProcessor.lockControls();
            		angleFinder.findPath();
			return;
		}

        	// process path
		if(processor != null && !processor.isDone())
		{
			processor.goToGoal();
			return;
		}

        angleFinder = null;
        PathProcessor.releaseControls();    
	}
	
	private boolean breakBlocks(List<BlockPos> blocks) //changed
	{   
        	return blocks.stream().filter(this::breakBlock).findFirst().map(pos -> {currentBlock = pos; return true;}).orElse(false);
	}
	
	private boolean breakBlock(BlockPos pos)
	{
        double rangeSq = range.getValueSq();
        BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(pos);
        if (params == null)
            return false;

	if(params.distanceSq() > rangeSq)
        {
            return false;
        }
			
        if(checkBreakLOS.isChecked() && !params.lineOfSight())
        {
            return false;
        }

        // face block
	    facing.getSelected().face(params.hitVec());

        // damage block and swing hand
        if(MC.interactionManager.updateBlockBreakingProgress(pos, params.side()))
            swingHand.getSelected().swing(Hand.MAIN_HAND);

		return true;  
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		PathCmd pathCmd = WURST.getCmds().pathCmd;

        	if(mining != null)
			mining.draw(matrixStack);

		if(miningFinder != null)
			miningFinder.renderPath(matrixStack, pathCmd.isDebugMode(), pathCmd.isDepthTest());
		
		if(angleFinder != null)
			angleFinder.renderPath(matrixStack, pathCmd.isDebugMode(), pathCmd.isDepthTest());
	}
	
	private List<BlockPos> getNeighbors(BlockPos pos)
	{
        	int sz = boxRange.getValueI();
		BlockPos mp = pos.add(-sz,-sz,-sz);
		BlockPos MP = pos.add(sz, sz, sz);
		return BlockUtils.getAllInBoxStream(mp, MP).filter(MiningBotUtils::isLog).collect(Collectors.toList());
	}
	
	private abstract class MiningBotPathFinder extends PathFinder
	{
        	public MiningBotPathFinder(MiningBotPathFinder pathFinder)
		{
			super(pathFinder);
		}

		public MiningBotPathFinder(BlockPos goal)
		{
			super(goal);
		}

		public void findPath()
		{
			think();
			
			if(isDoneOrFailed())
			{
				// set processor
				formatPath();
				processor = new MiningBotPathProcessor(this);
			}
		}
		
		public boolean isDoneOrFailed()
		{
			return isDone() || isFailed();
		}

        	public abstract void reset();      
	}
	
	private class MiningBotPathProcessor
	{
        	private final PathProcessor processor;
		private final MiningBotPathFinder pathFinder;

		public MiningBotPathProcessor(MiningBotPathFinder pathFinder)
		{
			this.pathFinder = pathFinder;
			processor = pathFinder.getProcessor();
		}
		
		 public void goToGoal()
		 {
            		if(!pathFinder.isPathStillValid(processor.getIndex()) || processor.getTicksOffPath() > 20)
			{
				pathFinder.reset();
				return;
			}

            		if(processor.canBreakBlocks() && breakBlocks(getLeavesOnPath()))
				return;

            		if(mining != null && breakBlocks(mining.getLogs()))
				return;

            		processor.process();
		 }

        	private List<BlockPos> getLeavesOnPath() //changed
		{
			List<PathPos> path = pathFinder.getPath();
			path = path.subList(processor.getIndex(), path.size());

			return path.stream().flatMap(pos -> Stream.of(pos, pos.up(height.getValueI())))
				.distinct().filter(MiningBotUtils::isLeaves)
				.collect(Collectors.toList());
		}

		public boolean isDone()
		{
			return processor.isDone();
		}
	}
	
	private class MiningFinder extends MiningBotPathFinder
	{
		public MiningFinder()
		{
			super(BlockPos.ofFloored(MC.player.getPos()));
		}
		
		public MiningFinder(MiningBotPathFinder pathFinder)
		{
			super(pathFinder);
		}

        	@Override
		protected boolean isMineable(BlockPos pos)
		{
			return MiningBotUtils.isLeaves(pos);
		}

        	@Override
		protected boolean checkDone()
		{
			return done = isNextToMiningStump(current);
		}

		private boolean isNextToMiningStump(PathPos pos)
		{
			return isMiningStump(pos.north()) || isMiningStump(pos.south()) 
                 	|| isMiningStump(pos.west()) || isMiningStump(pos.east())
                 	|| isMiningStump(pos.up()) || isMiningStump(pos.down());
		}
		
		private boolean isMiningStump(BlockPos pos)
		{
			if(MiningBotUtils.isLog(pos) || !TreeBotUtils.isLog(pos.down()))
			{
				analyzeMining(pos);
				return true;
			} 
               		return false;
		}

    private void analyzeMining(BlockPos stump) 
    {
        List<BlockPos> logs = new ArrayList<>(Arrays.asList(stump));
        Set<BlockPos> visited = new HashSet<>(Arrays.asList(stump));
        Deque<BlockPos> queue = new ArrayDeque<>(Arrays.asList(stump));
        BlockPos current = queue.pollFirst();

        for (BlockPos next : getNeighbors(current)) 
        {
            if (queue.size() >= queueSize.getValueI()) break;
            if (logs.size() >= arraySize.getValueI()) break;

            if (!visited.contains(next))
            {
                visited.add(next);
                logs.add(next);
                queue.add(next);
            }
        }
        mining = new Mining(stump, logs);
    }

        	@Override
		public void reset()
		{
			miningFinder = new MiningFinder(miningFinder);
		}
	}
	
	private class AngleFinder extends MiningBotPathFinder
	{
        	public AngleFinder()
		{
			super(BlockPos.ofFloored(MC.player.getPos()));
		}

        	public AngleFinder(MiningBotPathFinder pathFinder)
		{
			super(pathFinder);
		}

        	@Override
		protected boolean isMineable(BlockPos pos)
		{
			return MiningBotUtils.isLeaves(pos);
		}

        	@Override
		protected boolean checkDone()
		{
			return done = hasAngle(current);
		}


		private boolean hasAngle(PathPos pos)
		{
			double rangeSq = range.getValueSq();
			ClientPlayerEntity player = MC.player;
			Vec3d eyes = Vec3d.ofBottomCenter(pos).add(0, player.getEyeHeight(player.getPose()), 0);
			
			for(BlockPos log : mining.getLogs())
			{
				BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(eyes, log);
                		if(params == null)
                		{
                    			return false;
                		}

                		if(params.distanceSq() > rangeSq)
                		{
                    			return false;
                		}
                    
               			if(checkAngleLOS.isChecked() && !params.lineOfSight())
                		{
                    			return false;
                		}      
			}
			
			return true;
		}

        	@Override
		public void reset()
		{
			angleFinder = new AngleFinder(angleFinder);
		}
    }		
}
