package com.github.lunatrius.schematica.command.client;

import com.github.lunatrius.schematica.block.state.pattern.BlockStateReplacer;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CommandSchematicaReplace extends CommandSchematicaBase {
	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
		return Commands.literal(Names.Command.Replace.NAME)
		               .then(Commands.argument("toReplace", BlockStateArgument.block(context))
		                             .then(Commands.argument("with", BlockStateArgument.block(context)))
		                             .executes((commandContext) -> {
			                             CommandSourceStack source = commandContext.getSource();
			                             BlockState toReplace =
					                             BlockStateArgument.getBlock(commandContext, "toReplace").getState();

			                             BlockState with =
					                             BlockStateArgument.getBlock(commandContext, "with").getState();

			                             SchematicWorld schematic = ClientProxy.schematic;

			                             if (schematic == null) {
				                             source.sendFailure(Component.translatable(
						                             Names.Command.Replace.Message.NO_SCHEMATIC));
				                             return -1;
			                             }

			                             try {
				                             BlockStateMatchTest matcher = new BlockStateMatchTest(toReplace);
				                             BlockStateReplacer replacer = BlockStateReplacer.forBlockState(with);
				                             int count = schematic.replaceBlock(matcher, replacer);

				                             source.sendSuccess(
						                             () -> Component.translatable(Names.Command.Replace.Message.SUCCESS,
						                                                          count), true);
			                             } catch (Exception e) {
				                             Reference.logger.error("Something went wrong!", e);
				                             source.sendFailure(Component.literal(e.getMessage()));
				                             return -1;
			                             }
			                             return 0;
		                             }));
	}
}