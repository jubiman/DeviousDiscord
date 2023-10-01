package com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.commands;

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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

public class TempbanIpCommand {
	private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType(new TranslatableComponent("commands.banip.invalid"));
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableComponent("commands.banip.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("devious").then(Commands.literal("admin").requires(p -> p.hasPermission(3))
				.then(Commands.literal("tempban-ip").requires(p -> p.hasPermission(3))
						.then(Commands.argument("target", StringArgumentType.word())
								.then(Commands.argument("months", IntegerArgumentType.integer(0))
										.then(Commands.argument("days", IntegerArgumentType.integer(0))
												.then(Commands.argument("hours", IntegerArgumentType.integer(0))
														.executes(TempbanIpCommand::tempbanUsernameOrIp)
														.then(Commands.argument("reason", MessageArgument.message())
																.executes(TempbanIpCommand::tempbanUsernameOrIp)
														)
												)
										)
								)
						)
				)
		));
	}

	private static int tempbanUsernameOrIp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		return tempbanUsernameOrIp(ctx.getSource(), StringArgumentType.getString(ctx, "target"), IntegerArgumentType.getInteger(ctx, "months"), IntegerArgumentType.getInteger(ctx, "days"), IntegerArgumentType.getInteger(ctx, "hours"), MessageArgument.getMessage(ctx, "reason"));
	}

	private static int tempbanUsernameOrIp(CommandSourceStack source, String username, int monthDuration, int dayDuration, int hourDuration, Component reason) throws CommandSyntaxException {
		Matcher matcher = BanIpCommands.IP_ADDRESS_PATTERN.matcher(username);
		if (matcher.matches()) {
			return tempbanIpAddress(source, username, monthDuration, dayDuration, hourDuration, reason);
		} else {
			ServerPlayer serverplayer = source.getServer().getPlayerList().getPlayerByName(username);
			if (serverplayer != null) {
				return tempbanIpAddress(source, serverplayer.getIpAddress(), monthDuration, dayDuration, hourDuration, reason);
			} else {
				throw ERROR_INVALID_IP.create();
			}
		}
	}

	private static int tempbanIpAddress(CommandSourceStack source, String ip, int monthDuration, int dayDuration, int hourDuration, Component reason) throws CommandSyntaxException {
		IpBanList ipbanlist = source.getServer().getPlayerList().getIpBans();
		Date date = DateUtils.addMonths(DateUtils.addDays(DateUtils.addHours(new Date(), hourDuration), dayDuration), monthDuration);

		if (ipbanlist.isBanned(ip)) {
			throw FAILED_EXCEPTION.create();
		} else {
			List<ServerPlayer> list = source.getServer().getPlayerList().getPlayersWithAddress(ip);
			IpBanListEntry ipbanentry = new IpBanListEntry(ip,null, source.getTextName(), date, reason == null ? null : reason.getString());
			ipbanlist.add(ipbanentry);
			source.sendSuccess(new TranslatableComponent("Banned %s for %s months, %s days and %s hours: %s", ip, monthDuration, dayDuration, hourDuration, ipbanentry.getReason()), true);
			if (!list.isEmpty()) {
				source.sendSuccess(new TranslatableComponent("commands.banip.info", list.size(), EntitySelector.joinNames(list)), true);
			}

			for(ServerPlayer serverplayerentity : list) {
				serverplayerentity.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.ip_banned"));
			}

			return list.size();
		}
	}
}

