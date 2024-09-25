/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.map.MapDecoration;

import net.wurstclient.WurstClient;
import net.wurstclient.hack.HackList;

@Mixin(MapRenderer.MapTexture.class)
private abstract class MapRendererMixin {
    @ModifyExpressionValue(method = "draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ZI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/map/MapState;getDecorations()Ljava/lang/Iterable;"))
    private Iterable<MapDecoration> getIconsProxy(Iterable<MapDecoration> original) {
      HackList hax = WurstClient.INSTANCE.getHax();
        return (hax.noMapOverlayHack.isEnabled() && hax.noMapOverlayHack.noMapMarkers.isChecked()) ? Iterator::new : original;
    }

    @Inject(method = "draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ZI)V", at = @At("HEAD"), cancellable = true)
    private void onDraw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, boolean hidePlayerIcons, int light, CallbackInfo ci) {
      HackList hax = WurstClient.INSTANCE.getHax();
        if (hax.noMapOverlayHack.isEnabled() && hax.noMapOverlayHack.noMapContents.isChecked()) 
          ci.cancel();
    }
}
