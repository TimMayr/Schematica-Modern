package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CommandSchematicaSave extends CommandSchematicaBase {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal(Names.Command.Save.NAME)
				.then(Commands.argument("from", BlockPosArgument.blockPos())
						.then(Commands.argument("to", BlockPosArgument.blockPos())
								.then(Commands.argument("name", StringArgumentType.string())
										.executes(CommandSchematicaSave::execute)
										.then(Commands.argument("format",
														StringArgumentType.string())
												.suggests(((context, builder) -> {
													for (String s :
															SchematicFormat.FORMATS.keySet()) {
														builder.suggest(s);
													}
													return builder.buildFuture();
												}))
												.executes(CommandSchematicaSave::execute)
												.then(Commands.argument("isPrivate",
																BoolArgumentType.bool())
														.executes(CommandSchematicaSave::execute)
														.then(Commands.argument("override",
																		BoolArgumentType.bool())
																.executes(CommandSchematicaSave::execute)))))));
	}

	private static int execute(@NotNull CommandContext<CommandSourceStack> commandContext)
			throws CommandSyntaxException {
		CommandSourceStack source = commandContext.getSource();
		Player player = source.getPlayerOrException();
		BlockPos fromBlock = BlockPosArgument.getBlockPos(commandContext, "from");
		BlockPos toBlock = BlockPosArgument.getBlockPos(commandContext, "to");
		String name = StringArgumentType.getString(commandContext, "name");
		boolean confirm = false;
		String format = "Alpha";
		boolean isPrivate = true;

		try {
			format = StringArgumentType.getString(commandContext, "format");
		} catch (IllegalArgumentException ignored) {
		}

		try {
			isPrivate = BoolArgumentType.getBool(commandContext, "isPrivate");
		} catch (IllegalArgumentException ignored) {
		}

		try {
			confirm = BoolArgumentType.getBool(commandContext, "override");
		} catch (IllegalArgumentException ignored) {
		}

		if (Reference.proxy.isPlayerQuotaExceeded(player)) {
			source.sendFailure(Component.translatable(Names.Command.Save.Message.QUOTA_EXCEEDED));
			return -1;
		}

		MBlockPos from = new MBlockPos(fromBlock);
		MBlockPos to = new MBlockPos(toBlock);

		if (!SchematicFormat.FORMATS.containsKey(format)) {
			source.sendFailure(Component.translatable(Names.Command.Save.Message.UNKNOWN_FORMAT));
			return -1;
		}

		String filename = name + SchematicFormat.getExtension(format);

		File directory = Reference.proxy.getPlayerSchematicDirectory(player, isPrivate);
		File file = new File(directory, filename);

		if (!confirm && file.exists()) {
			String confirmCommand =
					String.format("/%s %s %s %s %s %s %b %b", Names.Command.BASE, Names.Command.Save.NAME, from, to,
							name, format, isPrivate, true);
			Component chatComponent =
					Component.translatable(Names.Command.Save.Message.CONFIRM_MESSAGE, name)
							.append(Component.literal(" "))
							.append(withStyle(ComponentUtils.wrapInSquareBrackets(
											Component.translatable(Names.Command.Remove.Message.YES)),
									ChatFormatting.RED, confirmCommand));

			source.sendSystemMessage(chatComponent);
			return 0;
		}

		Reference.logger.debug("Saving schematic from {} to {} to {}", from, to, filename);
		File schematicDirectory = Reference.proxy.getPlayerSchematicDirectory(player, isPrivate);
		if (schematicDirectory == null) {
			Reference.logger.warn("Unable to determine the schematic directory for player {}", player);
			source.sendFailure(Component.translatable(Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE));
			return -1;
		}

		try {
			Reference.proxy.saveSchematic(player, filename, player.getCommandSenderWorld(), format,
					from, to, isPrivate, "");
		} catch (Exception e) {
			source.sendFailure(Component.translatable(Names.Command.Save.Message.SAVE_FAILED));
			return -1;
		}
		return 0;
	}
}