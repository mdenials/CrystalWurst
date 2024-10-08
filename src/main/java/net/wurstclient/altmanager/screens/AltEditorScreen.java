/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.altmanager.screens;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.wurstclient.WurstClient;
import net.wurstclient.altmanager.NameGenerator;

public abstract class AltEditorScreen extends Screen
{	
	protected final Screen prevScreen;
	
	private TextFieldWidget nameOrEmailBox;
	private TextFieldWidget passwordBox;
	
	private ButtonWidget doneButton;
	
	protected String message = "";
	
	public AltEditorScreen(Screen prevScreen, Text title)
	{
		super(title);
		this.prevScreen = prevScreen;
	}
	
	@Override
	public final void init()
	{
		addDrawableChild(doneButton = ButtonWidget
			.builder(Text.literal(getDoneButtonText()), b -> pressDoneButton())
			.dimensions(width / 2 - 100, height / 4 + 72 + 12, 200, 20)
			.build());
		
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Cancel"), b -> client.setScreen(prevScreen))
			.dimensions(width / 2 - 100, height / 4 + 120 + 12, 200, 20)
			.build());
		
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Random Name"),
				b -> nameOrEmailBox.setText(NameGenerator.generateName()))
			.dimensions(width / 2 - 100, height / 4 + 96 + 12, 200, 20)
			.build());
		
		nameOrEmailBox = new TextFieldWidget(textRenderer, width / 2 - 100, 60,
			200, 20, Text.literal(""));
		nameOrEmailBox.setMaxLength(48);
		nameOrEmailBox.setFocused(true);
		nameOrEmailBox.setText(getDefaultNameOrEmail());
		addSelectableChild(nameOrEmailBox);
		
		passwordBox = new TextFieldWidget(textRenderer, width / 2 - 100, 100,
			200, 20, Text.literal(""));
		passwordBox.setText(getDefaultPassword());
		passwordBox.setRenderTextProvider((text, int_1) -> {
			String stars = "";
			for(int i = 0; i < text.length(); i++)
				stars += "*";
			return OrderedText.styledForwardsVisitedString(stars, Style.EMPTY);
		});
		passwordBox.setMaxLength(256);
		addSelectableChild(passwordBox);
		
		setFocused(nameOrEmailBox);
	}
	
	@Override
	public final void tick()
	{
		String nameOrEmail = nameOrEmailBox.getText().trim();
		doneButton.active = !(nameOrEmail.isEmpty() && passwordBox.getText().isEmpty());
	}
	
	/**
	 * @return the user-entered name or email. Cannot be empty when pressing the
	 *         done button. Cannot be null.
	 */
	protected final String getNameOrEmail()
	{
		return nameOrEmailBox.getText();
	}
	
	/**
	 * @return the user-entered password. Can be empty. Cannot be null.
	 */
	protected final String getPassword()
	{
		return passwordBox.getText();
	}
	
	protected String getDefaultNameOrEmail()
	{
		return client.getSession().getUsername();
	}
	
	protected String getDefaultPassword()
	{
		return "";
	}
	
	protected abstract String getDoneButtonText();
	
	protected abstract void pressDoneButton();
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int int_3)
	{
		if(keyCode == GLFW.GLFW_KEY_ENTER)
			doneButton.onPress();
		
		return super.keyPressed(keyCode, scanCode, int_3);
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int button)
	{
		nameOrEmailBox.mouseClicked(x, y, button);
		passwordBox.mouseClicked(x, y, button);
		
		if(nameOrEmailBox.isFocused() || passwordBox.isFocused())
			message = "";
		
		return super.mouseClicked(x, y, button);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		renderBackground(context, mouseX, mouseY, partialTicks);
		
		MatrixStack matrixStack = context.getMatrices();
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		
		// text
		context.drawTextWithShadow(textRenderer, "Name (for cracked alts), or",
			width / 2 - 100, 37, 10526880);
		context.drawTextWithShadow(textRenderer, "E-Mail (for premium alts)",
			width / 2 - 100, 47, 10526880);
		context.drawTextWithShadow(textRenderer,
			"Password (leave blank for cracked alts)", width / 2 - 100, 87,
			10526880);
		
		String[] lines = message.split("\n");
		for(int i = 0; i < lines.length; i++)
			context.drawCenteredTextWithShadow(textRenderer, lines[i],
				width / 2, 142 + 10 * i, 16777215);
		
		// text boxes
		nameOrEmailBox.render(context, mouseX, mouseY, partialTicks);
		passwordBox.render(context, mouseX, mouseY, partialTicks);
		
		for(Drawable drawable : drawables)
			drawable.render(context, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public final void close()
	{
		client.setScreen(prevScreen);
	}
}
