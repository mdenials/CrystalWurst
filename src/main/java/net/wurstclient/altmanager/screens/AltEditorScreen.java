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
import net.wurstclient.altmanager.AltRenderer;
import net.wurstclient.altmanager.NameGenerator;

public abstract class AltEditorScreen extends Screen
{	
	protected final Screen prevScreen;
	
	private TextFieldWidget nameOrEmailBox;
	private TextFieldWidget passwordBox;
	
	private ButtonWidget doneButton;
	
	protected String message = "";
	private int errorTimer;
	
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
	
	protected final void doErrorEffect()
	{
		errorTimer = 8;
	}
	
	/**
	 * Grabs the JSON code from the session server. It looks something like
	 * this:
	 *
	 * <code><pre>
	 * {
	 *   "id": "&lt;UUID&gt;",
	 *   "name": "&lt;username&gt;",
	 *   "properties":
	 *   [
	 *     {
	 *       "name": "textures",
	 *       "value": "&lt;base64 encoded JSON&gt;"
	 *     }
	 *   ]
	 * }
	 * </pre></code>
	 */
	private JsonObject getSessionJson(String uuid) throws IOException
	{
		URL sessionURL = URI
			.create(
				"https://sessionserver.mojang.com/session/minecraft/profile/")
			.resolve(uuid).toURL();
		
		try(InputStream sessionInputStream = sessionURL.openStream())
		{
			return new Gson().fromJson(
				IOUtils.toString(sessionInputStream, StandardCharsets.UTF_8),
				JsonObject.class);
		}
	}
	
	private String getUUID(String username) throws IOException
	{
		URL profileURL =
			URI.create("https://api.mojang.com/users/profiles/minecraft/")
				.resolve(URLEncoder.encode(username, "UTF-8")).toURL();
		
		try(InputStream profileInputStream = profileURL.openStream())
		{
			// {"name":"<username>","id":"<UUID>"}
			
			JsonObject profileJson = new Gson().fromJson(
				IOUtils.toString(profileInputStream, StandardCharsets.UTF_8),
				JsonObject.class);
			
			return profileJson.get("id").getAsString();
		}
	}
	
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
		
		// skin preview
		AltRenderer.drawAltBack(context, nameOrEmailBox.getText(),
			(width / 2 - 100) / 2 - 64, height / 2 - 128, 128, 256);
		AltRenderer.drawAltBody(context, nameOrEmailBox.getText(),
			width - (width / 2 - 100) / 2 - 64, height / 2 - 128, 128, 256);
		
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
		
		// red flash for errors
		if(errorTimer > 0)
		{
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_BLEND);
			
			RenderSystem.setShaderColor(1, 0, 0, errorTimer / 16F);
			
			BufferBuilder bufferBuilder = tessellator
				.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
			bufferBuilder.vertex(matrix, 0, 0, 0);
			bufferBuilder.vertex(matrix, width, 0, 0);
			bufferBuilder.vertex(matrix, width, height, 0);
			bufferBuilder.vertex(matrix, 0, height, 0);
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			errorTimer--;
		}
		
		for(Drawable drawable : drawables)
			drawable.render(context, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public final void close()
	{
		client.setScreen(prevScreen);
	}
}
