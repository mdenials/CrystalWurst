/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;

import net.wurstclient.WurstClient;
import net.wurstclient.hack.HackList;

@Mixin(SignBlockEntityRenderer.class)
public abstract class SignBlockEntityRendererMixin {
    @ModifyExpressionValue(method = "renderText", at = @At(value = "CONSTANT", args = {"intValue=4", "ordinal=1"}))
    private int loopTextLengthProxy(int i) {
        HackList hax = WurstClient.INSTANCE.getHax();
        if (hax.noSignOverlayHack.isEnabled() && hax.noSignOverlayHack.noSignText.isChecked()) 
            return 0;
        return i;
    }
}
