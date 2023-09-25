package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.ModConfig;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.DeviousDiscord;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.ChannelHandler;
import org.java_websocket.WebSocket;

public class MessageEvent implements Event {

	@Override
	public void handle(WebSocket webSocket, JsonObject json) {
		if (json.has("channel") && json.get("channel").getAsString().equals("global")) {
			ChannelHandler.sendMessageToGlobalChannel(
					ModConfig.getGlobalMessageFormat().replaceAll("(?<!\\\\)%s", ModConfig.getIdentifier())
							.replaceAll("(?<!\\\\)%d", json.get("serverName").getAsString())
							.replaceAll("(?<!\\\\)%u", json.get("username").getAsString())
							.replaceAll("(?<!\\\\)%m", json.get("message").getAsString()));
		} else if (json.has("channel") && json.get("channel").getAsString().equals("server")) {
			ChannelHandler.sendMessageToServerChannel(
					ModConfig.getServerMessageFormat().replaceAll("(?<!\\\\)%s", ModConfig.getIdentifier())
							.replaceAll("(?<!\\\\)%u", json.get("username").getAsString())
							.replaceAll("(?<!\\\\)%m", json.get("message").getAsString()));
		} else {
			DeviousDiscord.LOGGER
					.debug("Received unknown message from Devious Socket: " + json.get("message").getAsString());
		}
	}
}
