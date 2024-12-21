package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.client.gui.control.GuiSchematicControl;
import com.github.lunatrius.schematica.client.gui.load.GuiSchematicLoad;
import com.github.lunatrius.schematica.client.gui.save.GuiSchematicSave;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.client.world.SchematicWorld.LayerMode;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class InputHandler {
	public static final InputHandler INSTANCE = new InputHandler();
	private static final KeyBinding KEY_BINDING_LOAD =
			new KeyBinding(Names.Keys.LOAD, GLFW.GLFW_KEY_KP_DIVIDE, Names.Keys.CATEGORY);
	private static final KeyBinding KEY_BINDING_SAVE =
			new KeyBinding(Names.Keys.SAVE, GLFW.GLFW_KEY_KP_MULTIPLY, Names.Keys.CATEGORY);
	private static final KeyBinding KEY_BINDING_CONTROL =
			new KeyBinding(Names.Keys.CONTROL, GLFW.GLFW_KEY_KP_SUBTRACT, Names.Keys.CATEGORY);
	private static final KeyBinding KEY_BINDING_LAYER_INC =
			new KeyBinding(Names.Keys.LAYER_INC, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyBinding KEY_BINDING_LAYER_DEC =
			new KeyBinding(Names.Keys.LAYER_DEC, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyBinding KEY_BINDING_LAYER_TOGGLE =
			new KeyBinding(Names.Keys.LAYER_TOGGLE, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyBinding KEY_BINDING_RENDER_TOGGLE =
			new KeyBinding(Names.Keys.RENDER_TOGGLE, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyBinding KEY_BINDING_PRINTER_TOGGLE =
			new KeyBinding(Names.Keys.PRINTER_TOGGLE, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyBinding KEY_BINDING_MOVE_HERE =
			new KeyBinding(Names.Keys.MOVE_HERE, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	private static final KeyBinding KEY_BINDING_PICK_BLOCK =
			new KeyBinding(Names.Keys.PICK_BLOCK, KeyConflictContext.IN_GAME, KeyModifier.SHIFT,
			               InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, Names.Keys.CATEGORY);
	public static final KeyBinding[] KEY_BINDINGS = new KeyBinding[] {KEY_BINDING_LOAD,
	                                                                  KEY_BINDING_SAVE,
	                                                                  KEY_BINDING_CONTROL,
	                                                                  KEY_BINDING_LAYER_INC,
	                                                                  KEY_BINDING_LAYER_DEC,
	                                                                  KEY_BINDING_LAYER_TOGGLE,
	                                                                  KEY_BINDING_RENDER_TOGGLE,
	                                                                  KEY_BINDING_PRINTER_TOGGLE,
	                                                                  KEY_BINDING_MOVE_HERE,
	                                                                  KEY_BINDING_PICK_BLOCK};

	private final Minecraft minecraft = Minecraft.getInstance();

	private InputHandler() {}

	@SubscribeEvent
	public void onKeyInput(InputEvent event) {
		if (this.minecraft.currentScreen == null) {
			if (KEY_BINDING_LOAD.isPressed()) {
				this.minecraft.displayGuiScreen(new GuiSchematicLoad(this.minecraft.currentScreen));
			}

			if (KEY_BINDING_SAVE.isPressed()) {
				this.minecraft.displayGuiScreen(new GuiSchematicSave(this.minecraft.currentScreen));
			}

			if (KEY_BINDING_CONTROL.isPressed()) {
				this.minecraft.displayGuiScreen(new GuiSchematicControl(this.minecraft.currentScreen));
			}

			if (KEY_BINDING_LAYER_INC.isPressed()) {
				SchematicWorld schematic = ClientProxy.schematic;
				if (schematic != null && schematic.layerMode != LayerMode.ALL) {
					schematic.renderingLayer =
							MathHelper.clamp(schematic.renderingLayer + 1, 0, schematic.getHeight() - 1);
				}
			}

			if (KEY_BINDING_LAYER_DEC.isPressed()) {
				SchematicWorld schematic = ClientProxy.schematic;
				if (schematic != null && schematic.layerMode != LayerMode.ALL) {
					schematic.renderingLayer =
							MathHelper.clamp(schematic.renderingLayer - 1, 0, schematic.getHeight() - 1);
				}
			}

			if (KEY_BINDING_LAYER_TOGGLE.isPressed()) {
				SchematicWorld schematic = ClientProxy.schematic;
				if (schematic != null) {
					schematic.layerMode = LayerMode.next(schematic.layerMode);
				}
			}

			if (KEY_BINDING_RENDER_TOGGLE.isPressed()) {
				SchematicWorld schematic = ClientProxy.schematic;
				if (schematic != null) {
					schematic.isRendering = !schematic.isRendering;
				}
			}

			if (KEY_BINDING_PRINTER_TOGGLE.isPressed()) {
				if (ClientProxy.schematic != null) {
					boolean printing = SchematicPrinter.INSTANCE.togglePrinting();
					if (this.minecraft.player != null) {
						this.minecraft.player.sendMessage(new TranslationTextComponent(Names.Messages.TOGGLE_PRINTER,
						                                                               I18n.format(printing
						                                                                           ? Names.Gui.ON
						                                                                           : Names.Gui.OFF)));
					}
				}
			}

			if (KEY_BINDING_MOVE_HERE.isPressed()) {
				SchematicWorld schematic = ClientProxy.schematic;
				if (schematic != null) {
					ClientProxy.moveSchematicToPlayer(schematic);
				}
			}

			if (KEY_BINDING_PICK_BLOCK.isPressed()) {
				SchematicWorld schematic = ClientProxy.schematic;
				if (schematic != null && schematic.isRendering) {
					pickBlock(schematic, ClientProxy.objectMouseOver);
				}
			}
		}
	}

	private void pickBlock(SchematicWorld schematic, RayTraceResult objectMouseOver) {
		// Minecraft.func_147112_ai
		if (objectMouseOver == null) {
			return;
		}

		if (objectMouseOver.getType() == RayTraceResult.Type.MISS) {
			return;
		}

		ClientPlayerEntity player = this.minecraft.player;
		if (player != null && !ForgeHooks.onPickBlock(objectMouseOver, player, schematic)) {
			return;
		}

		if (player != null && player.isCreative()) {
			int slot = player.inventory.mainInventory.size() - 10 + player.inventory.currentItem;
			if (this.minecraft.playerController != null) {
				this.minecraft.playerController.sendSlotPacket(
						player.inventory.getStackInSlot(player.inventory.currentItem), slot);
			}
		}

	}
}