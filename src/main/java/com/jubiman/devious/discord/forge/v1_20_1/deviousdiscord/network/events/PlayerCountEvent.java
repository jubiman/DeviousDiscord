package com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.WebSocketConnection;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PlayerCountEvent implements Event {
	@Override
	public void handle(WebSocketConnection webSocket, JsonObject json) {
		JsonObject out = new JsonObject();
		out.addProperty("event", "playerCount");
		out.addProperty("count", ServerLifecycleHooks.getCurrentServer().getPlayerCount());
		webSocket.sendJson(out);
	}
}
