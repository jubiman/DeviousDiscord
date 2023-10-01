package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord;

import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.ChannelType;
import net.minecraftforge.common.config.Config;

@Config(modid = DeviousDiscord.MOD_ID)
public class ModConfig {
	@Config.Comment("Network configuration")
	public static NetworkConfig networkConfig = new NetworkConfig();

	public static class NetworkConfig {
		@Config.Comment("The identifier (aka server name) to use when connecting to the server.")
		public String identifier = "UNKNOWN";

		@Config.Comment("The port of the server to connect to.")
		@Config.RangeInt(min = 0, max = 65535)
		public int port = 8080;

		@Config.Comment("The hostname of the server to connect to.")
		public String host = "127.0.0.1";

		@Config.Comment("The interval (in seconds) to wait before attempting to reconnect to the server.")
		@Config.RangeInt(min = 1, max = 3600)
		public int reconnectInterval = 3600;
	}

	@Config.Comment("Message configuration")
	public static MessageConfig messageConfig = new MessageConfig();

	public static class MessageConfig {
		@Config.Comment({"The format of the message sent to the Devious Socket.", "Placeholders: %s = server name, %u = username, %m = message. Use \\\\ to escape the placeholders."})
		public String serverMessageFormat = "§1§l[%s] §4§u<%u>§r %m";
		@Config.Comment({"The format of the message sent to the Devious Socket.", "Placeholders: %s = server name, %u = username, %m = message %d = Receiving Server Name. Use \\\\ to escape the placeholders."})
		public String globalMessageFormat = "§1§l[%d] §4§u<%u>§r %m";

		@Config.Comment("The default channel a player is connected to on this server.")
		public ChannelType defaultChannel = ChannelType.SERVER;
	}

	public static String getIdentifier() {
		return networkConfig.identifier;
	}

	public static int getPort() {
		return networkConfig.port;
	}

	public static String getHostname() {
		return networkConfig.host;
	}

	public static long getReconnectInterval() {
		return networkConfig.reconnectInterval;
	}

	public static String getServerMessageFormat() {
		return messageConfig.serverMessageFormat;
	}

	public static String getGlobalMessageFormat() {
		return messageConfig.globalMessageFormat;
	}

	public static ChannelType getDefaultChannel() {
		return messageConfig.defaultChannel;
	}
}
