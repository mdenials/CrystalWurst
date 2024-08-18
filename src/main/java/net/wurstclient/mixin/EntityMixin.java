/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.wurstclient.WurstClient;
import net.wurstclient.hack.HackList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.EntityLike;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.VelocityFromEntityCollisionListener.VelocityFromEntityCollisionEvent;
import net.wurstclient.events.VelocityFromFluidListener.VelocityFromFluidEvent;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable, EntityLike, CommandOutput
{
	@Shadow
	private Box boundingBox;
	@Shadow
	private float yaw;
	@Shadow
	private float pitch;
	@Shadow
	public float prevYaw;
	@Shadow
	public float prevPitch;

	/**
	 * This mixin makes the VelocityFromFluidEvent work, which is used by
	 * AntiWaterPush. It's set to require 0 because it doesn't work in Forge,
	 * when using Sinytra Connector.
	 */
	@WrapWithCondition(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V",
		opcode = Opcodes.INVOKEVIRTUAL,
		ordinal = 0),
		method = "updateMovementInFluid(Lnet/minecraft/registry/tag/TagKey;D)Z",
		require = 0)
	private boolean shouldSetVelocity(Entity instance, Vec3d velocity)
	{
		VelocityFromFluidEvent event = new VelocityFromFluidEvent(instance);
		EventManager.fire(event);
		return !event.isCancelled();
	}
	
	@Inject(at = @At("HEAD"),
		method = "Lnet/minecraft/entity/Entity;pushAwayFrom(Lnet/minecraft/entity/Entity;)V",
		cancellable = true)
	private void onPushAwayFrom(Entity entity, CallbackInfo ci)
	{
		VelocityFromEntityCollisionEvent event =
			new VelocityFromEntityCollisionEvent((Entity)(Object)this);
		EventManager.fire(event);
		
		if(event.isCancelled())
			ci.cancel();
	}
	
	/**
	 * Makes invisible entities render as ghosts if TrueSight is enabled.
	 */
	@Inject(at = @At("RETURN"),
		method = "Lnet/minecraft/entity/Entity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z",
		cancellable = true)
	private void onIsInvisibleTo(PlayerEntity player,
		CallbackInfoReturnable<Boolean> cir)
	{
		// Return early if the entity is not invisible
		if(!cir.getReturnValueZ())
			return;
		
		if(WurstClient.INSTANCE.getHax().trueSightHack
			.shouldBeVisible((Entity)(Object)this))
			cir.setReturnValue(false);
	}

@Inject(method = "Lnet/minecraft/entity/Entity;changeLookDirection", at = @At("HEAD"), cancellable = true)
   private void overridePitch(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci)
    {
      if (WurstClient.INSTANCE.getHax().noPitchLimitHack.isEnabled())
        {
         if (MathHelper.abs(this.pitch) > 180.0F)
         {
            this.pitch = (float)(-MathHelper.sign((double)this.pitch) * 180) + (this.pitch - (float)(MathHelper.sign((double)this.pitch) * 180));
            this.prevPitch = (float)(-MathHelper.sign((double)this.prevPitch) * 180) + (this.prevPitch - (float)(MathHelper.sign((double)this.prevPitch) * 180));
         }

         float changePitch = (float)cursorDeltaY * 0.15F;
         float changeYaw = (float)cursorDeltaX * 0.15F;
         this.pitch += changePitch;
         this.yaw += MathHelper.abs(this.pitch) % 360.0F > 90.0F ? -changeYaw : changeYaw;
         this.prevPitch += changePitch;
         this.prevYaw += MathHelper.abs(this.pitch) % 360.0F > 90.0F ? -changeYaw : changeYaw;
         ci.cancel();
      }
   }

 @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;getBoundingBox", cancellable = true)
    public final void onGetBoundingBox(CallbackInfoReturnable<Box> cir) {
    HackList hax = WurstClient.INSTANCE.getHax();
    if (WurstClient.INSTANCE.getHax().hitboxHack.shouldBeExpand((Entity)(Object)this))
    {
	cir.setReturnValue(new Box(
	this.boundingBox.minX - hax.hitboxHack.sv.getValue()/2f,
	this.boundingBox.minY - hax.hitboxHack.hv.getValue()/2f,
	this.boundingBox.minZ - hax.hitboxHack.sv.getValue()/2f,
	this.boundingBox.maxX + hax.hitboxHack.sv.getValue()/2f,
	this.boundingBox.maxY + hax.hitboxHack.hv.getValue()/2f,
	this.boundingBox.maxZ + hax.hitboxHack.sv.getValue()/2f));
        }
    }
	
}
