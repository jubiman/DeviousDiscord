package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.commands;

import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.DeviousDiscord;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.ChannelHandler;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.permission.PermissionAPI;
import org.jline.utils.Levenshtein;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;


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
		return "/devious channel global|server|none";
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
				throw new CommandException("Expected 2 arguments, got " + args.length + "! Usage: " + getUsage(sender));
			case 2: {
				if (Objects.equals(args[0], "admin")) {
					// if the sender is the console or has permission
					if (sender instanceof MinecraftServer
							|| PermissionAPI.hasPermission((EntityPlayer) sender,
							"deviousdiscord.admin")) {
						if (Objects.equals(args[1], "reconnect")) {
							DeviousDiscord.INSTANCE.connection.reconnect();
							sender.sendMessage(new TextComponentString("Successfully reconnected to  Devious Socket!"));
						} else {
							throw new CommandException("Invalid arguments! Usage: " + getUsage(sender));
						}
					} else {
						throw new CommandException("You do not have permission to use this command!");
					}
				} else if (Objects.equals(args[0], "channel")) {
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
				} else {
					throw new CommandException("Invalid arguments! Usage: " + getUsage(sender));
				}
				break;
			}
			default:
				throw new CommandException("Invalid arguments! Usage: " + getUsage(sender));
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
					suggestions.add("reconnect");
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
			default:
				return suggestions;
		}
	}
}
