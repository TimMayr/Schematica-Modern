package com.github.lunatrius.schematica.command.client;

import com.github.lunatrius.schematica.block.state.pattern.BlockStateReplacer;
import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;

@Environment(EnvType.CLIENT)
public class CommandSchematicaReplace extends CommandSchematicaBase {
	public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> register(CommandBuildContext context) {
		return ClientCommandRegistrationEvent.literal(Names.Command.BASE).then(
				ClientCommandRegistrationEvent.literal(Names.Command.Replace.NAME)
						.then(ClientCommandRegistrationEvent.argument("toReplace", BlockStateArgument.block(context))
								.then(ClientCommandRegistrationEvent.argument("with",
												BlockStateArgument.block(context))
										.executes((commandContext) -> {
											ClientCommandRegistrationEvent.ClientCommandSourceStack source =
													commandContext.getSource();

											BlockState toReplace = commandContext.getArgument("toReplace",
													BlockInput.class).getState();
											BlockState with =
													commandContext.getArgument("with", BlockInput.class).getState();

											FakeLevel schematic = ClientProxy.schematic;

											if (schematic == null) {
												source.arch$sendFailure(Component.translatable(
														Names.Command.Replace.Message.NO_SCHEMATIC));
												return -1;
											}

											try {
												BlockStateMatchTest matcher = new BlockStateMatchTest(toReplace);
												BlockStateReplacer replacer = BlockStateReplacer.forBlockState(with);
												int count = schematic.replaceBlock(matcher, replacer);

												source.arch$sendSuccess(
														() -> Component.translatable(Names.Command.Replace.Message.SUCCESS,
																count), true);
											} catch (Exception e) {
												Reference.logger.error("Something went wrong!", e);
												source.arch$sendFailure(Component.literal(e.getMessage()));
												return -1;
											}
											return 0;
										}))));
	}
}