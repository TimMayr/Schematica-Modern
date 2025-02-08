package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.core.util.math.MathHelper;
import com.github.lunatrius.schematica.client.gui.control.SchematicControlScreen;
import com.github.lunatrius.schematica.client.gui.save.SchematicSaveScreen;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

public class InputHandler {
	public static final InputHandler INSTANCE = new InputHandler();
	private static final KeyMapping KEY_BINDING_LOAD =
			new KeyMapping(Names.Keys.LOAD, GLFW.GLFW_KEY_KP_DIVIDE, Names.Keys.CATEGORY);
	private static final KeyMapping KEY_BINDING_SAVE =
			new KeyMapping(Names.Keys.SAVE, InputConstants.KEY_MULTIPLY, Names.Keys.CATEGORY);
	private static final KeyMapping KEY_BINDING_CONTROL =
			new KeyMapping(Names.Keys.CONTROL, GLFW.GLFW_KEY_KP_SUBTRACT, Names.Keys.CATEGORY);
	private static final KeyMapping KEY_BINDING_LAYER_INC =
			new KeyMapping(Names.Keys.LAYER_INC, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyMapping KEY_BINDING_LAYER_DEC =
			new KeyMapping(Names.Keys.LAYER_DEC, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyMapping KEY_BINDING_LAYER_TOGGLE =
			new KeyMapping(Names.Keys.LAYER_TOGGLE, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyMapping KEY_BINDING_RENDER_TOGGLE =
			new KeyMapping(Names.Keys.RENDER_TOGGLE, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyMapping KEY_BINDING_PRINTER_TOGGLE =
			new KeyMapping(Names.Keys.PRINTER_TOGGLE, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyMapping KEY_BINDING_MOVE_HERE =
			new KeyMapping(Names.Keys.MOVE_HERE, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyMapping KEY_BINDING_PICK_BLOCK =
			new KeyMapping(Names.Keys.PICK_BLOCK, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	public static final Codec<InputHandler> CODEC = Codec.unit(InputHandler::new);
	public static final KeyMapping[] KEY_BINDINGS = new KeyMapping[] {KEY_BINDING_LOAD,
	                                                                  KEY_BINDING_SAVE,
	                                                                  KEY_BINDING_CONTROL,
	                                                                  KEY_BINDING_LAYER_INC,
	                                                                  KEY_BINDING_LAYER_DEC,
	                                                                  KEY_BINDING_LAYER_TOGGLE,
	                                                                  KEY_BINDING_RENDER_TOGGLE,
	                                                                  KEY_BINDING_PRINTER_TOGGLE,
	                                                                  KEY_BINDING_MOVE_HERE,
	                                                                  KEY_BINDING_PICK_BLOCK};

	private InputHandler() {
		ClientTickEvent.CLIENT_POST.register(instance -> {
			if (instance.screen == null) {
				if (KEY_BINDING_LOAD.isDown()) {
					instance.setScreen(new com.github.lunatrius.schematica.client.gui.load.SchematicLoadScreen(instance.screen));
				}

				if (KEY_BINDING_SAVE.isDown()) {
					instance.setScreen(new SchematicSaveScreen(instance.screen));
				}

				if (KEY_BINDING_CONTROL.isDown()) {
					instance.setScreen(new SchematicControlScreen(instance.screen));
				}

				if (KEY_BINDING_LAYER_INC.isDown()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null && schematic.layerMode != FakeLevel.LayerMode.ALL) {

						schematic.renderLayer =
								MathHelper.clamp(schematic.renderLayer + 1, 0, schematic.getHeight() - 1);
					}
				}

				if (KEY_BINDING_LAYER_DEC.isDown()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null && schematic.layerMode != FakeLevel.LayerMode.ALL) {
						schematic.renderLayer =
								MathHelper.clamp(schematic.renderLayer - 1, 0, schematic.getHeight() - 1);
					}
				}

				if (KEY_BINDING_LAYER_TOGGLE.isDown()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null) {
						schematic.layerMode = FakeLevel.LayerMode.next(schematic.layerMode);
					}
				}

				if (KEY_BINDING_RENDER_TOGGLE.isDown()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null) {
						schematic.setRendering(!schematic.isRendering());
					}
				}

				if (KEY_BINDING_PRINTER_TOGGLE.isDown()) {
					if (ClientProxy.schematic != null) {
						boolean printing = SchematicPrinter.INSTANCE.togglePrinting();
						if (instance.player != null) {
							instance.player.displayClientMessage(Component.translatable(Names.Messages.TOGGLE_PRINTER,
							                                                            printing
							                                                            ? Names.Gui.ON
							                                                            : Names.Gui.OFF), false);
						}
					}
				}

				if (KEY_BINDING_MOVE_HERE.isDown()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null) {
						ClientProxy.moveSchematicToPlayer(schematic);
					}
				}

				if (KEY_BINDING_PICK_BLOCK.isDown()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null && schematic.isRendering()) {
						pickBlock(schematic, ClientProxy.objectMouseOver, instance);
					}
				}
			}
		});
	}

	private void pickBlock(FakeLevel schematic, HitResult objectMouseOver, Minecraft instance) {
		if (objectMouseOver == null) {
			return;
		}

		if (objectMouseOver.getType() == HitResult.Type.MISS) {
			return;
		}

		LocalPlayer player = instance.player;

		if (player != null && player.isCreative()) {
			int slot = player.getInventory().items.size() - 10 + player.getInventory().selected;
			if (instance.gameMode != null) {
				instance.gameMode.handlePickItemFromBlock(new MBlockPos(objectMouseOver.getLocation()),
				                                          player.input.keyPresses.sprint());
			}
		}
	}
}