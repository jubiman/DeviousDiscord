package com.jubiman.devious.discord.forge.v1_19_2.deviousdiscord.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.List;

public class TempbanIpCommand {
	private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType(Component.translatable("commands.banip.invalid"));
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.banip.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("devious").then(Commands.literal("admin").requires(p -> p.hasPermission(3))
				.then(Commands.literal("tempban-ip").requires(p -> p.hasPermission(3))
						.then(Commands.argument("target", StringArgumentType.word())
								.then(Commands.argument("months", IntegerArgumentType.integer(0))
										.then(Commands.argument("days", IntegerArgumentType.integer(0))
												.then(Commands.argument("hours", IntegerArgumentType.integer(0))
														.executes(ctx -> tempbanIpOrName(ctx, null))
														.then(Commands.argument("reason", MessageArgument.message())
																.executes(ctx -> tempbanIpOrName(ctx, MessageArgument.getMessage(ctx, "reason")))
														)
												)
										)
								)
						)
				)
		));
	}

	private static int tempbanIpOrName(CommandContext<CommandSourceStack> ctx, Component reason) throws CommandSyntaxException {
		return tempbanIpOrName(ctx.getSource(), StringArgumentType.getString(ctx, "target"), IntegerArgumentType.getInteger(ctx, "months"), IntegerArgumentType.getInteger(ctx, "days"), IntegerArgumentType.getInteger(ctx, "hours"), reason);
	}

	private static int tempbanIpOrName(CommandSourceStack source, String username, int monthDuration, int dayDuration, int hourDuration, Component reason) throws CommandSyntaxException {
		if (InetAddresses.isInetAddress(username))
			return tempbanIpAddress(source, username, monthDuration, dayDuration, hourDuration, reason);
		else {
			ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(username);

			if (player != null)
				return tempbanIpAddress(source, player.getIpAddress(), monthDuration, dayDuration, hourDuration, reason);
			else
				throw ERROR_INVALID_IP.create();
		}
	}

	private static int tempbanIpAddress(CommandSourceStack source, String ip, int monthDuration, int dayDuration, int hourDuration, Component reason) throws CommandSyntaxException {
		IpBanList ipbanlist = source.getServer().getPlayerList().getIpBans();
		Date date = DateUtils.addMonths(DateUtils.addDays(DateUtils.addHours(new Date(), hourDuration), dayDuration), monthDuration);

		if (ipbanlist.isBanned(ip))
			throw FAILED_EXCEPTION.create();
		else {
			List<ServerPlayer> list = source.getServer().getPlayerList().getPlayersWithAddress(ip);
			IpBanListEntry ipbanentry = new IpBanListEntry(ip, null, source.getTextName(), date, reason == null ? null : reason.getString());

			ipbanlist.add(ipbanentry);
			source.sendSuccess(Component.translatable("Banned %s for %s months, %s days and %s hours: %s", ip, monthDuration, dayDuration, hourDuration, ipbanentry.getReason()), true);

			if (!list.isEmpty())
				source.sendSuccess(Component.translatable("commands.banip.info", list.size(), EntitySelector.joinNames(list)), true);

			for (ServerPlayer player : list) {
				player.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned"));
			}

			return list.size();
		}
	}
}

