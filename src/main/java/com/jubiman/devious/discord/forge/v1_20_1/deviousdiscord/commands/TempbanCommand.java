package com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Collection;
import java.util.Date;

public class TempbanCommand {
	private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.ban.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("devious").then(Commands.literal("admin").requires((player) -> player.hasPermission(3))
				.then(Commands.literal("tempban")
						.then(Commands.argument("targets", GameProfileArgument.gameProfile())
								.then(Commands.argument("months", IntegerArgumentType.integer(0))
										.then(Commands.argument("days", IntegerArgumentType.integer(0))
												.then(Commands.argument("hours", IntegerArgumentType.integer(0))
														.executes(ctx -> tempbanPlayers(ctx, null))
														.then(Commands.argument("reason", MessageArgument.message())
																.executes(ctx -> tempbanPlayers(ctx, MessageArgument.getMessage(ctx, "reason")))
														)
												)
										)
								)
						)
				)
		));
	}

	private static int tempbanPlayers(CommandContext<CommandSourceStack> ctx, Component message) throws CommandSyntaxException {
		return tempbanPlayers(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "months"), IntegerArgumentType.getInteger(ctx, "days"), IntegerArgumentType.getInteger(ctx, "hours"), message);
	}

	private static int tempbanPlayers(CommandSourceStack source, Collection<GameProfile> toBeBanned, int monthDuration, int dayDuration, int hourDuration, Component reason) throws CommandSyntaxException {
		UserBanList banList = source.getServer().getPlayerList().getBans();
		int i = 0;
		Date date = DateUtils.addMonths(DateUtils.addDays(DateUtils.addHours(new Date(), hourDuration), dayDuration), monthDuration);

		for (GameProfile gameprofile : toBeBanned) {
			if (!banList.isBanned(gameprofile)) {
				UserBanListEntry banListEntry = new UserBanListEntry(gameprofile, null, source.getTextName(), date, reason == null ? null : reason.getString());

				banList.add(banListEntry);
				++i;
				source.sendSuccess(() -> Component.translatable("Banned %s for %s months, %s days and %s hours: %s", ComponentUtils.getDisplayName(gameprofile), monthDuration, dayDuration, hourDuration, banListEntry.getReason()), true);

				ServerPlayer player = source.getServer().getPlayerList().getPlayer(gameprofile.getId());

				if (player != null)
					player.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
			}
		}

		if (i == 0)
			throw ERROR_ALREADY_BANNED.create();
		else
			return i;
	}
}
