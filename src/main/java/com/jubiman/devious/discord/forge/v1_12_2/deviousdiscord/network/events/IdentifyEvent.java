package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.ModConfig;
import org.java_websocket.WebSocket;

public class IdentifyEvent implements Event {
	@Override
	public void handle(WebSocket webSocket, JsonObject json) {
		JsonObject out = new JsonObject();
		out.addProperty("event", "identify");
		out.addProperty("identifier", ModConfig.getIdentifier());
		webSocket.send(out.toString());
	}
}
