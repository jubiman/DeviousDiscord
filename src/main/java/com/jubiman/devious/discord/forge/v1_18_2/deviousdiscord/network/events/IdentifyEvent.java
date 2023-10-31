package com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.Config;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.WebSocketConnection;

public class IdentifyEvent implements Event {
	@Override
	public void handle(WebSocketConnection connection, JsonObject json) {
		JsonObject out = new JsonObject();
		out.addProperty("event", "identify");
		out.addProperty("identifier", Config.getIdentifier());
		connection.sendJson(out);
	}
}
