package com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord;

import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.ChannelType;
import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
	static final ForgeConfigSpec SPEC;
	private static final ForgeConfigSpec.ConfigValue<String> hostname;
	private static final ForgeConfigSpec.IntValue port;
	private static final ForgeConfigSpec.ConfigValue<String> identifier;
	private static final ForgeConfigSpec.EnumValue<ChannelType> defaultChannel;
	private static final ForgeConfigSpec.ConfigValue<String> serverMessageFormat;
	private static final ForgeConfigSpec.ConfigValue<String> globalMessageFormat;
	private static final ForgeConfigSpec.IntValue reconnectInterval;
	public static final Database database;

	public static final class Database {
		private final ForgeConfigSpec.ConfigValue<String> hostname;
		private final ForgeConfigSpec.IntValue port;
		private final ForgeConfigSpec.ConfigValue<String> username;
		private final ForgeConfigSpec.ConfigValue<String> password;
		private final ForgeConfigSpec.ConfigValue<String> database;

		Database(ForgeConfigSpec.Builder builder) {
			builder.comment("The hostname of the database to connect to.");
			hostname = builder.define("hostname", "localhost");
			builder.comment("The port of the database to connect to.");
			port = builder.defineInRange("port", 3306, 0, 65535);
			builder.comment("The username to use when connecting to the database.");
			username = builder.define("username", "root");
			builder.comment("The password to use when connecting to the database.");
			password = builder.define("password", "password");
			builder.comment("The database to use when connecting to the database.");
			database = builder.define("database", "deviousdiscord");
		}

		public String getHostname() {
			return hostname.get();
		}

		public int getPort() {
			return port.get();
		}

		public String getUsername() {
			return username.get();
		}

		public String getPassword() {
			return password.get();
		}

		public String getDatabase() {
			return database.get();
		}
	}

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		{
			builder.push("server");
			{
				builder.comment("The hostname of the server to connect to.");
				hostname = builder.define("hostname", "127.0.0.1");
				builder.comment("The port of the server to connect to.");
				port = builder.defineInRange("port", 8080, 0, 65535);
				builder.comment("The identifier (aka server name) to use when connecting to the server.");
				identifier = builder.define("identifier", "UNKNOWN");
				builder.comment("The default channel a player is connected to on this server.");
				defaultChannel = builder.defineEnum("default_channel", ChannelType.SERVER);
				builder.comment("The format of the message sent to the Devious Socket.");
				builder.comment(
						"Placeholders: %s = server name, %u = username, %m = message. Use \\\\ to escape the placeholders.");
				serverMessageFormat = builder.define("server_message_format", "§1§l[%s] §4§u<%u>§r %m");
				builder.comment("The format of the message sent to the Devious Socket.");
				builder.comment(
						"Placeholders: %s = server name, %u = username, %m = message %d = Receiving Server Name. Use \\\\ to escape the placeholders.");
				globalMessageFormat = builder.define("global_message_format", "§1§l[%d] §4§u<%u>§r %m");
				builder.comment("The interval in seconds to wait before reconnecting to the Devious Socket.");
				// 5 minutes default, 1 second minimum, 1 hour maximum
				reconnectInterval = builder.defineInRange("reconnect_interval", 5 * 60, 1, 60 * 60);
				builder.pop();
			}
			builder.push("database");
			{
				database = new Database(builder);
				builder.pop();
			}
		}
		SPEC = builder.build();
	}

	public static String getHostname() {
		return hostname.get();
	}

	public static int getPort() {
		return port.get();
	}

	public static String getIdentifier() {
		return identifier.get();
	}

	public static ChannelType getDefaultChannel() {
		return defaultChannel.get();
	}

	public static String getServerMessageFormat() {
		return serverMessageFormat.get();
	}

	public static String getGlobalMessageFormat() {
		return globalMessageFormat.get();
	}

	public static int getReconnectInterval() {
		return reconnectInterval.get();
	}
}
