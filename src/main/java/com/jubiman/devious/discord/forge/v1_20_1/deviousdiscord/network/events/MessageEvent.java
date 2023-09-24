package com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.Config;
import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.DeviousDiscord;
import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.ChannelHandler;

import java.net.http.WebSocket;

public class MessageEvent implements Event {

	@Override
	public void handle(WebSocket webSocket, JsonObject json) {
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
