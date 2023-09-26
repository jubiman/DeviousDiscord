package com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord;

import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.ChannelHandler;
import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.WebSocketConnection;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.jline.utils.Levenshtein;
import org.slf4j.Logger;

@Mod(DeviousDiscord.MODID)
public class DeviousDiscord {
	public static final String MODID = "deviousdiscord";
	public static final Logger LOGGER = LogUtils.getLogger();
	private WebSocketConnection connection;

	public DeviousDiscord() {

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		// Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
	}

	@SubscribeEvent
	public void onCommandsRegister(RegisterCommandsEvent event) {
		// Register command to reconnect to the websocket, only works if the user has the permission level of OP
		event.getDispatcher().register(Commands.literal("devious")
				.then(Commands.literal("admin")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("reconnect")
				.executes(context -> {
					try {
						this.connection.reconnect();
						context.getSource().sendSuccess(() -> Component.literal(Config.getIdentifier() + " reconnected to Devious Socket"), false);
					} catch (Exception e) {
						LOGGER.warn("Failed to reconnect to Devious Socket", e);
						context.getSource().sendFailure(Component.literal("Failed to reconnect to Devious Socket! (See server logs for more info)"));
						return -1;
					}
					return 1;
		}))));
		event.getDispatcher().register(Commands.literal("devious")
				.then(Commands.literal("channel")
				.then(Commands.argument("channel", StringArgumentType.word())
						.suggests((context, builder) -> {
							try {
								String arg = context.getArgument("channel", String.class);
								// Predict which channel the user wants to join with the current (incomplete) input
								int gDist = Levenshtein.distance(arg, "global");
								int sDist = Levenshtein.distance(arg, "server");
								int nDist = Levenshtein.distance(arg, "none  "); // Added to make it equal length xD

								if (gDist < sDist && gDist < nDist) {
									builder.suggest("global");
								} else if (sDist < gDist && sDist < nDist) {
									builder.suggest("server");
								} else if (nDist < gDist && nDist < sDist) {
									builder.suggest("none");
								} else {
									builder.suggest("global");
									builder.suggest("server");
									builder.suggest("none");
								}
							} catch (IllegalArgumentException e) {
								builder.suggest("global");
								builder.suggest("server");
								builder.suggest("none");
							}
							return builder.buildFuture();
						})
						.executes(context -> {
							switch (StringArgumentType.getString(context, "channel").toLowerCase()) {
								case "global" ->
										ChannelHandler.addGlobalChannel(context.getSource().getPlayerOrException().getUUID());
								case "server" ->
										ChannelHandler.addServerChannel(context.getSource().getPlayerOrException().getUUID());
								case "none" -> // TODO: rename this?
										ChannelHandler.removeChannel(context.getSource().getPlayerOrException().getUUID());
								default -> {
									context.getSource().sendFailure(Component.literal("Invalid channel type!"));
									return -1;
								}
							}
							context.getSource().sendSuccess(() -> Component.literal("Joined channel " + StringArgumentType.getString(context, "channel")), false);
							return 1;
						}))));
	}

	@SubscribeEvent
	public void onServerStarted(ServerStartedEvent event) {
		LOGGER.info("Trying to connect to Devious Socket");
		try {
			connection = new WebSocketConnection();
		} catch (Exception e) {
			LOGGER.warn("Failed to connect to Devious Socket", e);
		}
	}

	@SubscribeEvent
	public void onChatMessage(ServerChatEvent event) {
		connection.sendMessage(event.getUsername(), event.getPlayer().getUUID(), event.getMessage());
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		LOGGER.info("Closing Devious Socket connection");
		connection.close("Server is closing connection");
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		connection.sendPlayerEvent(event.getEntity().getName().getString(), event.getEntity().getUUID(), true);

		switch (Config.getDefaultChannel()) {
			case GLOBAL -> ChannelHandler.addGlobalChannel(event.getEntity().getUUID());
			case SERVER -> ChannelHandler.addServerChannel(event.getEntity().getUUID());
		}
	}

	@SubscribeEvent
	public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
		connection.sendPlayerEvent(event.getEntity().getName().getString(), event.getEntity().getUUID(), true);

		ChannelHandler.removeChannel(event.getEntity().getUUID());
	}
}
