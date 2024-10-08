/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.ai;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.util.RegionPos;

public final class PathRenderer
{
	public static void renderArrow(MatrixStack matrixStack, BlockPos start, BlockPos end, RegionPos region)
	{
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);
		
		int startX = start.getX() - region.x();
		int startY = start.getY();
		int startZ = start.getZ() - region.z();
		
		int endX = end.getX() - region.x();
		int endY = end.getY();
		int endZ = end.getZ() - region.z();
		
		matrixStack.push();
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		
		// main line
		bufferBuilder.vertex(matrix, startX, startY, startZ);
		bufferBuilder.vertex(matrix, endX, endY, endZ);
		
		matrixStack.pop();
		
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}
}
