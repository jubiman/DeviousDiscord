package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.commands;

import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.DeviousDiscord;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.ChannelHandler;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.permission.PermissionAPI;
import org.jline.utils.Levenshtein;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DeviousCommand extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getName() {
		return "devious";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// Get permission level of sender
		if (sender instanceof MinecraftServer
				|| PermissionAPI.hasPermission((EntityPlayer) sender,
				"deviousdiscord.admin")) {
			return "/devious <admin|channel> <args>";
		} else if (PermissionAPI.hasPermission((EntityPlayer) sender,
				"deviousdiscord.channel")) {
			return "/devious <channel> <args>";
		} else {
			return "You don't have permission to use this command!";
		}
	}

	@Override
	public List<String> getAliases() {
		return new ArrayList<>();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		switch (args.length) {
			case 0:
			case 1:
				throw new CommandException("Expected at least 2 arguments, got 1! Usage: " + getUsage(sender));
			default: {
				switch (args[0]) {
					case "admin":
						DeviousAdminCommand.execute(server, sender, args);
						break;
					case "channel":
						if (!(sender instanceof EntityPlayer)) {
							throw new CommandException("This command can only be used by players!");
						}
						UUID uuid = ((EntityPlayer) sender).getUniqueID();

						String channel = args[1];
						switch (channel) {
							case "global":
								ChannelHandler.addGlobalChannel(uuid);
								sender.sendMessage(new TextComponentString("Successfully joined global channel!"));
								break;
							case "server":
								ChannelHandler.addServerChannel(uuid);
								sender.sendMessage(new TextComponentString("Successfully joined server channel!"));
								break;
							case "none":
								ChannelHandler.removeChannel(uuid);
								sender.sendMessage(new TextComponentString("Successfully left all channels!"));
								break;
							default:
								throw new CommandException("Invalid channel! Valid channels are: global, server, none");
						}
						break;
					default:
						throw new CommandException("Invalid arguments! Usage: " + getUsage(sender));
				}
				break;
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		List<String> suggestions = new ArrayList<>();
		switch (args.length) {
			case 1: {
				suggestions.add("channel");
				// Get permission level of sender
				if (sender instanceof MinecraftServer
						|| PermissionAPI.hasPermission((EntityPlayer) sender,
						"deviousdiscord.admin")) {
					suggestions.add("admin");
				}
				return suggestions;
			}
			case 2: {
				if (args[0].equals("admin") && sender instanceof MinecraftServer
						|| PermissionAPI.hasPermission((EntityPlayer) sender,
						"deviousdiscord.admin")) {
					//suggestions.add("reconnect");
					String arg = args[1];
					// Predict which admin command the user wants to use with the current (incomplete) input
					int rDist = Levenshtein.distance(arg, "reconnect ");
					int tDist = Levenshtein.distance(arg, "tempban   ");
					int tiDist = Levenshtein.distance(arg, "tempban-ip");

					if (rDist < tDist && rDist < tiDist) {
						suggestions.add("reconnect");
					} else if (tDist < rDist && tDist < tiDist) {
						suggestions.add("tempban");
					} else if (tiDist < rDist && tiDist < tDist) {
						suggestions.add("tempban-ip");
					} else {
						suggestions.add("reconnect");
						suggestions.add("tempban");
						suggestions.add("tempban-ip");
					}
				} else if (args[0].equals("channel")) {
					try {
						String arg = args[1];
						// Predict which channel the user wants to join with the current (incomplete) input
						int gDist = Levenshtein.distance(arg, "global");
						int sDist = Levenshtein.distance(arg, "server");
						int nDist = Levenshtein.distance(arg, "none  "); // Added to make it equal length xD

						if (gDist < sDist && gDist < nDist) {
							suggestions.add("global");
						} else if (sDist < gDist && sDist < nDist) {
							suggestions.add("server");
						} else if (nDist < gDist && nDist < sDist) {
							suggestions.add("none");
						} else {
							suggestions.add("global");
							suggestions.add("server");
							suggestions.add("none");
						}
					} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
						suggestions.add("global");
						suggestions.add("server");
						suggestions.add("none");
					}
				}
				return suggestions;
			}
			case 3: {
				if (args[0].equals("admin") && sender instanceof MinecraftServer
						|| PermissionAPI.hasPermission((EntityPlayer) sender,
						"deviousdiscord.admin")) {
					if (args[1].equals("tempban")) {
						// Get all players on the server
						List<String> players = server.getPlayerList().getPlayers().stream().map(EntityPlayerMP::getName).collect(Collectors.toList());
						// Predict which player the user wants to ban with the current (incomplete) input
						String arg = args[2];
						int minDist = Integer.MAX_VALUE;
						String closest = "";
						for (String player : players) {
							int dist = Levenshtein.distance(arg, player);
							if (dist < minDist) {
								minDist = dist;
								closest = player;
							}
						}
						suggestions.add(closest);
					} else if (args[1].equals("tempban-ip")) {
						suggestions.add("<ip>");
					}
				}
				return suggestions;
			}
			case 4: {
				if (args[0].equals("admin") && sender instanceof MinecraftServer
						|| PermissionAPI.hasPermission((EntityPlayer) sender,
						"deviousdiscord.admin")) {
					if (args[1].equals("tempban") || args[1].equals("tempban-ip")) {
						suggestions.add("<months>");
					}
				}
				return suggestions;
			}
			case 5: {
				if (args[0].equals("admin") && sender instanceof MinecraftServer
						|| PermissionAPI.hasPermission((EntityPlayer) sender,
						"deviousdiscord.admin")) {
					if (args[1].equals("tempban") || args[1].equals("tempban-ip")) {
						suggestions.add("<days>");
					}
				}
				return suggestions;
			}
			case 6: {
				if (args[0].equals("admin") && sender instanceof MinecraftServer
						|| PermissionAPI.hasPermission((EntityPlayer) sender,
						"deviousdiscord.admin")) {
					if (args[1].equals("tempban") || args[1].equals("tempban-ip")) {
						suggestions.add("<hours>");
					}
				}
				return suggestions;
			}
			case 7: {
				if (args[0].equals("admin") && sender instanceof MinecraftServer
						|| PermissionAPI.hasPermission((EntityPlayer) sender,
						"deviousdiscord.admin")) {
					if (args[1].equals("tempban") || args[1].equals("tempban-ip")) {
						suggestions.add("[reason]");
					}
				}
				return suggestions;
			}
			default:
				return suggestions;
		}
	}
}
