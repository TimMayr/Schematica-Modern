package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ICommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSchematicaSave extends CommandSchematicaBase {

	public static ArgumentBuilder<CommandSource, ?> register() {
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
		                                                                       .executes(
				                                                                       CommandSchematicaSave::execute)))));
	}

	private static int execute(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {
		CommandSource source = commandContext.getSource();
		PlayerEntity player = source.asPlayer();
		BlockPos fromBlock = BlockPosArgument.getBlockPos(commandContext, "from");
		BlockPos toBlock = BlockPosArgument.getBlockPos(commandContext, "to");
		String name = StringArgumentType.getString(commandContext, "name");
		String format = "Alpha";

		try {
			format = StringArgumentType.getString(commandContext, "format");
		} catch (IllegalArgumentException ignored) {}

		if (Reference.proxy.isPlayerQuotaExceeded(player)) {
			throw new CommandException(new TranslationTextComponent(Names.Command.Save.Message.QUOTA_EXCEEDED));
		}

		MBlockPos from = new MBlockPos(fromBlock);
		MBlockPos to = new MBlockPos(toBlock);

		if (!SchematicFormat.FORMATS.containsKey(format)) {
			throw new CommandException(new TranslationTextComponent(Names.Command.Save.Message.UNKNOWN_FORMAT,
			                                                        format));
		}

		String filename = name + SchematicFormat.getExtension(format);

		Reference.logger.debug("Saving " + "schematic from {} to {} to {}", from, to, filename);
		File schematicDirectory = Reference.proxy.getPlayerSchematicDirectory(player, true);
		if (schematicDirectory == null) {
			//Chances are that if this is null, we could not retrieve their UUID.
			Reference.logger.warn("Unable to" + " determine the schematic directory for " + "player {}", player);
			throw new CommandException(
					new TranslationTextComponent(Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE));
		}

		if (!schematicDirectory.exists()) {
			if (!schematicDirectory.mkdirs()) {
				Reference.logger.warn("Could not create " + "player " + "schematic " + "directory " + "{}",
				                      schematicDirectory.getAbsolutePath());
				throw new CommandException(
						new TranslationTextComponent(Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE));
			}
		}

		try {
			Reference.proxy.saveSchematic(player, schematicDirectory, filename, player.getEntityWorld(), format, from,
			                              to);
			source.sendFeedback(new TranslationTextComponent(Names.Command.Save.Message.SAVE_SUCCESSFUL, name), true);
		} catch (Exception e) {
			throw new CommandException(new TranslationTextComponent(Names.Command.Save.Message.SAVE_FAILED, name));
		}
		return 0;
	}

	@Override
	public String getUsage(ICommandSource sender) {
		return Names.Command.Save.Message.USAGE;
	}
}