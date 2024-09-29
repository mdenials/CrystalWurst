/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.events;

import java.util.ArrayList;

import net.minecraft.util.shape.VoxelShape;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface BlockCollisionShapeListener extends Listener
{
	public void onBlockCollisionShape(BlockCollisionShapeEvent event);
	
	public static class BlockCollisionShapeEvent
		extends Event<BlockCollisionShapeListener>
	{
		private VoxelShape collisionShape;
		
		public VoxelShape getCollisionShape()
		{
			return collisionShape;
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
