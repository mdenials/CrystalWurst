/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;
import net.wurstclient.events.BlockCollisionShapeListener.BlockCollisionShapeEvent;
import net.wurstclient.event.EventManager;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.HackList;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin
{
  @Unique
    private static final Random RANDOM = new Random();
  
  @Inject(method = "getRenderingSeed", at = @At("HEAD"), cancellable = true)
    private void onRenderingSeed(BlockState state, BlockPos pos, CallbackInfoReturnable<Long> cir) {
      HackList hax = WurstClient.INSTANCE.getHax();
        if (hax.noTextureRotationsHack.isEnabled())
          cir.setReturnValue(RANDOM.nextLong());
    }

@Inject(at = @At("HEAD"),
		method = "getCollisionShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",
		cancellable = true)
	private void onGetCollisionShape(BlockState state, BlockView world,
		BlockPos pos, ShapeContext context,
		CallbackInfoReturnable<VoxelShape> cir)
	{
		BlockCollisionShapeEvent event = new BlockCollisionShapeEvent(pos, state.getBlock(), this.collidable ? state.getOutlineShape(world, pos) : VoxelShapes.empty());
		EventManager.fire(event);
		
		VoxelShape collisionShape = event.getCollisionShape();
		if(collisionShape != null)
			cir.setReturnValue(collisionShape);
	}
}
