/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.events;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface BlockCollisionShapeListener extends Listener
{
	public void onBlockCollisionShape(BlockPos pos, Block block, BlockCollisionShapeEvent event);
	
	public static class BlockCollisionShapeEvent
		extends Event<BlockCollisionShapeListener>
	{
		
		private BlockPos blockPos;
		private Block block;
		private VoxelShape collisionShape;
		
		public VoxelShape getCollisionBlockPos()
		{
			return blockPos;
		}

		public VoxelShape getCollisionBlock()
		{
			return block;
		}
		
		public VoxelShape getCollisionShape()
		{
			return collisionShape;
		}

		public void setBlockPos(BlockPos pos)
		{
			this.blockPos = blockPos;
		}

		public void setBlock(Block block)
		{
			this.block = block;
		}
		
		public void setCollisionShape(VoxelShape collisionShape)
		{
			this.collisionShape = collisionShape;
		}

		@Override
		public void fire(ArrayList<BlockCollisionShapeListener> listeners)
		{
			for(BlockCollisionShapeListener listener : listeners)
				listener.onBlockCollisionShape(this);
		}
		
		@Override
		public Class<BlockCollisionShapeListener> getListenerType()
		{
			return BlockCollisionShapeListener.class;
		}
	}
}
