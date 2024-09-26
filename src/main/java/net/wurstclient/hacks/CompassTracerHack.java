/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.awt.Color;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.TextFieldSetting;

public final class CompassTracerHack extends Hack implements RenderListener
{
private final ColorSetting color = new ColorSetting("Color","CompassTracer line and box will be highlighted in this color.", Color.RED);
private final TextFieldSetting xpos = new TextFieldSetting("X","0");
private final TextFieldSetting ypos= new TextFieldSetting("Y", "0");
private final TextFieldSetting zpos = new TextFieldSetting("Z", "0");

	public CompassTracerHack()
	{
		super("CompassTracer"); //"Draws a tracer to where your compass points to."
		setCategory(Category.RENDER);
        addSetting(color);
        addSetting(xpos);
        addSetting(ypos);
        addSetting(zpos);
	}

	@Override
	public void onEnable()
	{
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(RenderListener.class, this);
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if (MC.world == null) return;

        int x = Integer.parseInt(this.xpos.getValue());
        int y = Integer.parseInt(this.ypos.getValue());
        int z = Integer.parseInt(this.zpos.getValue());

		BlockPos spawn = new BlockPos(x,y,z);

		// GL settings
        float[] colorF = color.getColorF();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		matrixStack.push();
		RenderUtils.applyRenderOffset(matrixStack);
		color.setAsShaderColor();
		
		// box
		matrixStack.push();
		matrixStack.translate(spawn.getX(), spawn.getY(), spawn.getZ());
		RenderUtils.drawOutlinedBox(matrixStack);
		
		color.setAsShaderColor();
		RenderUtils.drawSolidBox(matrixStack);
		matrixStack.pop();
		
		// line
		Vec3d start = RotationUtils.getClientLookVec(partialTicks).add(RenderUtils.getCameraPos());
		Vec3d end = new Vec3d(spawn.getX()+0.5, spawn.getY()+0.5, spawn.getZ()+0.5);
		
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		Tessellator tessellator = RenderSystem.renderThreadTesselator();

		RenderSystem.setShader(GameRenderer::getPositionProgram);
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);
		bufferBuilder.vertex(matrix, (float)start.x, (float)start.y, (float)start.z);
		bufferBuilder.vertex(matrix, (float)end.x, (float)end.y, (float)end.z);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		
		matrixStack.pop();
		
		// GL resets
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
}
