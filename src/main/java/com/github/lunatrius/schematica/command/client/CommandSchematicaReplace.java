package com.github.lunatrius.schematica.command.client;

import com.github.lunatrius.schematica.block.state.pattern.BlockStateReplacer;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ICommandSource;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSchematicaReplace extends CommandSchematicaBase {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal(Names.Command.Replace.NAME)
				.then(Commands.argument("toReplace", BlockStateArgument.blockState())
						.then(Commands.argument("with", BlockStateArgument.blockState()).executes((commandContext) -> {
							CommandSource source = commandContext.getSource();
							BlockState toReplace =
									BlockStateArgument.getBlockState(commandContext, "toReplace").getState();

							BlockState with = BlockStateArgument.getBlockState(commandContext, "toReplace").getState();

							final SchematicWorld schematic = ClientProxy.schematic;

							if (schematic == null) {
								throw new CommandException(
										new TranslationTextComponent(Names.Command.Replace.Message.NO_SCHEMATIC));
							}

							try {
								final BlockStateMatcher matcher = BlockStateMatcher.forBlock(toReplace.getBlock());
								final BlockStateReplacer.BlockStateInfo properties =
										BlockStateReplacer.fromBlockState(with);
								final BlockStateReplacer replacer = BlockStateReplacer.forBlockState(with);
								final int count = schematic.replaceBlock(matcher, replacer, properties.stateData);

								source.sendFeedback(
										new TranslationTextComponent(Names.Command.Replace.Message.SUCCESS, count),
										true);
							} catch (final Exception e) {
								Reference.logger.error("Something went wrong!", e);
								throw new CommandException(new StringTextComponent(e.getMessage()));
							}
							return 0;
						}))));
	}

	@Override
	public String getUsage(final ICommandSource sender) {
		return Names.Command.Replace.Message.USAGE;
	}
}
