package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord;

import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.commands.DeviousCommand;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.ChannelHandler;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.WebSocketConnection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid= DeviousDiscord.MOD_ID, version = DeviousDiscord.VERSION, name = DeviousDiscord.NAME, serverSideOnly = true, acceptableRemoteVersions = "*")
public class DeviousDiscord {
	public static final String MOD_ID = "deviousdiscord";
	public static final String VERSION = "1.1.1";
	public static final String NAME = "Devious Discord";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public WebSocketConnection connection;

	@Mod.Instance
	public static DeviousDiscord INSTANCE;

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		PermissionAPI.registerNode("deviousdiscord.admin", DefaultPermissionLevel.OP, "Allows the user to use the /devious admin command");
		PermissionAPI.registerNode("deviousdiscord.channel", DefaultPermissionLevel.ALL, "Allows the user to use the /devious channel command");
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new DeviousCommand());
	}

	@EventHandler
	public void serverStarted(FMLServerStartedEvent event) {
		LOGGER.info("Trying to connect to Devious Socket");
		try {
			connection = new WebSocketConnection();
		} catch (Exception e) {
			LOGGER.warn("Failed to connect to Devious Socket", e);
		}
	}

	@SubscribeEvent
	public void onChatMessage(ServerChatEvent event) {
		connection.sendMessage(event.getUsername(), event.getPlayer().getUniqueID(), event.getMessage());
	}

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		LOGGER.info("Trying to disconnect from Devious Socket");
		try {
			connection.close("Server is closing connection");
		} catch (Exception e) {
			LOGGER.warn("Failed to disconnect from Devious Socket", e);
		}
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		this.connection.sendPlayerEvent(event.player.getName(), event.player.getUniqueID(), true);

		switch (ModConfig.getDefaultChannel()) {
			case GLOBAL: {
				ChannelHandler.addGlobalChannel(event.player.getUniqueID());
				break;
			}
			case SERVER: {
				ChannelHandler.addServerChannel(event.player.getUniqueID());
				break;
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
		this.connection.sendPlayerEvent(event.player.getName(), event.player.getUniqueID(), false);

		ChannelHandler.removeChannel(event.player.getUniqueID());
	}
}
