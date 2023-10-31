package com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.Config;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.DeviousDiscord;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.ChannelHandler;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.WebSocketConnection;

public class MessageEvent implements Event {

	@Override
	public void handle(WebSocketConnection connection, JsonObject json) {
		if (json.has("channel") && json.get("channel").getAsString().equals("global")) {
			ChannelHandler.sendMessageToGlobalChannel(
					Config.getGlobalMessageFormat().replaceAll("(?<!\\\\)%s", Config.getIdentifier())
							.replaceAll("(?<!\\\\)%d", json.get("serverName").getAsString())
							.replaceAll("(?<!\\\\)%u", json.get("username").getAsString())
							.replaceAll("(?<!\\\\)%m", json.get("message").getAsString()));
		} else if (json.has("channel") && json.get("channel").getAsString().equals("server")) {
			ChannelHandler.sendMessageToServerChannel(
					Config.getServerMessageFormat().replaceAll("(?<!\\\\)%s", Config.getIdentifier())
							.replaceAll("(?<!\\\\)%u", json.get("username").getAsString())
							.replaceAll("(?<!\\\\)%m", json.get("message").getAsString()));
		} else {
			DeviousDiscord.LOGGER
					.debug("Received unknown message from Devious Socket: " + json.get("message").getAsString());
		}
	}
}
