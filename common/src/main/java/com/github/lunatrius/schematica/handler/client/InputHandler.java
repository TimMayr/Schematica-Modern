package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.client.gui.control.SchematicControlScreen;
import com.github.lunatrius.schematica.client.gui.load.SchematicLoadScreen;
import com.github.lunatrius.schematica.client.gui.save.SchematicSaveScreen;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class InputHandler {
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
	public static final KeyMapping[] KEY_BINDINGS = new KeyMapping[]{KEY_BINDING_LOAD,
			KEY_BINDING_SAVE,
			KEY_BINDING_CONTROL,
			KEY_BINDING_LAYER_INC,
			KEY_BINDING_LAYER_DEC,
			KEY_BINDING_LAYER_TOGGLE,
			KEY_BINDING_RENDER_TOGGLE,
			KEY_BINDING_PRINTER_TOGGLE,
			KEY_BINDING_MOVE_HERE,
			KEY_BINDING_PICK_BLOCK};
	public static InputHandler INSTANCE;

	private InputHandler() {
		ClientTickEvent.CLIENT_PRE.register(instance -> {
			if (instance.screen == null) {
				while (KEY_BINDING_LOAD.consumeClick()) {
					instance.setScreen(new SchematicLoadScreen(instance.screen));
				}

				while (KEY_BINDING_SAVE.consumeClick()) {
					instance.setScreen(new SchematicSaveScreen(instance.screen));
				}

				while (KEY_BINDING_CONTROL.consumeClick()) {
					instance.setScreen(new SchematicControlScreen(instance.screen));
				}

				while (KEY_BINDING_LAYER_INC.isDown()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null && schematic.layerMode != FakeLevel.LayerMode.ALL) {

						schematic.renderLayer =
								Mth.clamp(schematic.renderLayer + 1, 0, schematic.getHeight() - 1);
					}
				}

				while (KEY_BINDING_LAYER_DEC.isDown()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null && schematic.layerMode != FakeLevel.LayerMode.ALL) {
						schematic.renderLayer =
								Mth.clamp(schematic.renderLayer - 1, 0, schematic.getHeight() - 1);
					}
				}

				while (KEY_BINDING_LAYER_TOGGLE.consumeClick()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null) {
						schematic.layerMode = FakeLevel.LayerMode.next(schematic.layerMode);
					}
				}

				while (KEY_BINDING_RENDER_TOGGLE.consumeClick()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null) {
						schematic.setRendering(!schematic.isRendering());
					}
				}

				while (KEY_BINDING_PRINTER_TOGGLE.consumeClick()) {
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

				while (KEY_BINDING_MOVE_HERE.consumeClick()) {
					FakeLevel schematic = ClientProxy.schematic;
					if (schematic != null) {
						ClientProxy.moveSchematicToPlayer(schematic);
					}
				}

				while (KEY_BINDING_PICK_BLOCK.consumeClick()) {
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
			if (instance.gameMode != null) {
				instance.gameMode.handlePickItemFromBlock(new MBlockPos(objectMouseOver.getLocation()),
						player.input.keyPresses.sprint());
			}
		}
	}

	public static void init() {
		InputHandler.INSTANCE = new InputHandler();
		for (KeyMapping keyBinding : InputHandler.KEY_BINDINGS) {
			KeyMappingRegistry.register(keyBinding);
		}
	}
}