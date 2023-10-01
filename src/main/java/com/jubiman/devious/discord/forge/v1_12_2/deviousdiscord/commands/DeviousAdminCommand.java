package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.commands;

import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.DeviousDiscord;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.management.UserListIPBans;
import net.minecraft.server.management.UserListIPBansEntry;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.commons.lang3.time.DateUtils;

import java.net.SocketAddress;
import java.util.Date;

public class DeviousAdminCommand {
	public static void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof MinecraftServer || PermissionAPI.hasPermission((EntityPlayer) sender, "deviousdiscord.admin")) {
			switch (args[1]) {
				case "reconnect":
					DeviousDiscord.INSTANCE.connection.reconnect();
					sender.sendMessage(new TextComponentString("Successfully reconnected to  Devious Socket!"));
					break;
				case "tempban": {
					if (args.length < 5) {
						throw new CommandException("Invalid arguments! Usage: /devious admin tempban <username> <months> <days> <hours> [reason]");
					}
					String username = args[2];
					int months = Integer.parseInt(args[3]);
					int days = Integer.parseInt(args[4]);
					int hours = Integer.parseInt(args[5]);
					String reason = args.length > 6 ? args[6] : null;

					Date date = DateUtils.addMonths(DateUtils.addDays(DateUtils.addHours(new Date(), hours), days), months);

					GameProfile gameProfile = server.getPlayerProfileCache().getGameProfileForUsername(username);
					if (gameProfile == null) {
						throw new CommandException("Player " + username + " not found!");
					}

					UserListBans banList = server.getPlayerList().getBannedPlayers();

					if (!banList.isBanned(gameProfile)) {
						UserListBansEntry banListEntry = new UserListBansEntry(gameProfile, null, sender.getName(), date, reason);

						banList.addEntry(banListEntry);
						sender.sendMessage(new TextComponentString(String.format("Banned %s for %s months, %s days and %s hours: %s", gameProfile.getName(), months, days, hours, reason)));

						EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(gameProfile.getId());

						player.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.banned"));
					} else {
						throw new CommandException("Player " + username + " is already banned!");
					}
					sender.sendMessage(new TextComponentString("Successfully sent tempban request to Devious Socket!"));
					break;
				}
				case "tempban-ip": {
					if (args.length < 5) {
						throw new CommandException("Invalid arguments! Usage: /devious admin tempban-ip <ip> <months> <days> <hours> [reason]");
					}
					SocketAddress ip = new java.net.InetSocketAddress(args[2], 25565);
					int months = Integer.parseInt(args[3]);
					int days = Integer.parseInt(args[4]);
					int hours = Integer.parseInt(args[5]);
					String reason = args.length > 6 ? args[6] : null;

					Date date = DateUtils.addMonths(DateUtils.addDays(DateUtils.addHours(new Date(), hours), days), months);

					UserListIPBans banList = server.getPlayerList().getBannedIPs();

					if (!banList.isBanned(ip)) {
						UserListIPBansEntry banListEntry = new UserListIPBansEntry(args[2], null, sender.getName(), date, reason);

						banList.addEntry(banListEntry);
						sender.sendMessage(new TextComponentString(String.format("Banned %s for %s months, %s days and %s hours: %s", ip, months, days, hours, reason)));
					} else {
						throw new CommandException("IP " + args[2] + " is already banned!");
					}
					sender.sendMessage(new TextComponentString("Successfully sent tempban-ip request to Devious Socket!"));
					break;
				}
				default:
					throw new CommandException("Invalid arguments! Usage: /devious admin <reconnect|tempban|tempban-ip>");
			}
		} else {
			throw new CommandException("You do not have permission to use this command!");
		}
	}
}
