package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

/**
 * Handles the global and server channels.
 */
public class ChannelHandler {
	private static final HashSet<UUID> globalChannel = new HashSet<>();
	private static final HashSet<UUID> serverChannel = new HashSet<>();

	/**
	 * Add a player to the global and server channel.
	 * @param uuid The player's UUID.
	 */
	public static void addGlobalChannel(UUID uuid) {
		// Global channel should receive messages from both global and server channels
		globalChannel.add(uuid);
		serverChannel.add(uuid);
	}

	/**
	 * Add a player to the server channel and remove them from the global channel.
	 * @param uuid The player's UUID.
	 */
	public static void addServerChannel(UUID uuid) {
		globalChannel.remove(uuid);
		serverChannel.add(uuid);
	}


	/**
	 * Remove a player from the global and server channel.
	 * @param uuid The player's UUID.
	 */
	public static void removeChannel(UUID uuid) {
		globalChannel.remove(uuid);
		serverChannel.remove(uuid);
	}

	/**
	 * Send a message to the global channel.
	 * @param message The message to send.
	 */
	public static void sendMessageToGlobalChannel(String message) {
		sendMessageOnChannel(message, globalChannel.iterator());
	}

	/**
	 * Send a message to the server channel.
	 * @param message The message to send.
	 */
	public static void sendMessageToServerChannel(String message) {
		sendMessageOnChannel(message, serverChannel.iterator());
	}

	/**
	 * Send a message to a channel.
	 * @param message The message to send.
	 * @param it The iterator of the channel to send the message to.
	 */
	private static void sendMessageOnChannel(String message, Iterator<UUID> it) {
		while (it.hasNext()) {
			UUID uuid = it.next();
			EntityPlayerMP player = FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(uuid);
			if (player == null) {
				// Remove the player from the global channel if they are not online
				it.remove();
				continue;
			}
			player.sendMessage(new TextComponentString(message));
		}
	}

	/**
	 * Get the channel a player is in.
	 * @param uuid The player's UUID.
	 * @return The channel the player is in.
	 */
	public static String getChannel(UUID uuid) {
		if (globalChannel.contains(uuid)) {
			return "global";
		} else if (serverChannel.contains(uuid)) {
			return "server";
		}
		return "none";
	}
}
