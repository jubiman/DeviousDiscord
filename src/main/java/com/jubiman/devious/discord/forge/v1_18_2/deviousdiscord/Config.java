package com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord;

import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.ChannelType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = DeviousDiscord.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec.ConfigValue<String> HOSTNAME = BUILDER
			.comment("The hostname of the server to connect to.")
			.define("hostname", "127.0.0.1");
	private static final ForgeConfigSpec.IntValue PORT = BUILDER
			.comment("The port of the server to connect to.")
			.defineInRange("port", 8080, 0, 65535);

	private static final ForgeConfigSpec.ConfigValue<String> IDENTIFIER = BUILDER
			.comment("The identifier (aka server name) to use when connecting to the server.")
			.define("identifier", "UNKNOWN");
	private static final ForgeConfigSpec.EnumValue<ChannelType> DEFAULT_CHANNEL = BUILDER
			.comment("The default channel a player is connected to on this server.")
			.defineEnum("default_channel", ChannelType.SERVER);
	private static final ForgeConfigSpec.ConfigValue<String> SERVER_MESSAGE_FORMAT = BUILDER
			.comment("The format of the message sent to the Devious Socket.")
			.comment(
					"Placeholders: %s = server name, %u = username, %m = message. Use \\\\ to escape the placeholders.")
			.define("server_message_format", "§1§l[%s] §4§u<%u>§r %m");
	private static final ForgeConfigSpec.ConfigValue<String> GLOBAL_MESSAGE_FORMAT = BUILDER
			.comment("The format of the message sent to the Devious Socket.")
			.comment(
					"Placeholders: %s = server name, %u = username, %m = message %d = Receiving Server Name. Use \\\\ to escape the placeholders.")
			.define("global_message_format", "§1§l[%d] §4§u<%u>§r %m");
	// 5 minutes default, 1 second minimum, 1 hour maximum
	private static final ForgeConfigSpec.IntValue RECONNECT_INTERVAL = BUILDER
			.comment("The interval in seconds to wait before reconnecting to the Devious Socket.")
			.defineInRange("reconnect_interval", 5 * 60, 1, 60 * 60);

	static final ForgeConfigSpec SPEC = BUILDER.build();
	private static String hostname;
	private static int port;
	private static String identifier;
	private static ChannelType defaultChannel;
	private static String serverMessageFormat;
	private static String globalMessageFormat;
	private static int reconnectInterval;

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		SPEC.isLoaded();

		hostname = HOSTNAME.get();
		port = PORT.get();
		identifier = IDENTIFIER.get();
		defaultChannel = DEFAULT_CHANNEL.get();
		serverMessageFormat = SERVER_MESSAGE_FORMAT.get();
		globalMessageFormat = GLOBAL_MESSAGE_FORMAT.get();
		reconnectInterval = RECONNECT_INTERVAL.get();
	}

	public static String getHostname() {
		return hostname;
	}

	public static int getPort() {
		return port;
	}

	public static String getIdentifier() {
		return identifier;
	}

	public static ChannelType getDefaultChannel() {
		return defaultChannel;
	}

	public static String getServerMessageFormat() {
		return serverMessageFormat;
	}

	public static String getGlobalMessageFormat() {
		return globalMessageFormat;
	}

	public static int getReconnectInterval() {
		return reconnectInterval;
	}
}
