package com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.net.http.WebSocket;

public class PlayerCountEvent implements Event {
	@Override
	public void handle(WebSocket webSocket, JsonObject json) {
		JsonObject out = new JsonObject();
		out.addProperty("event", "playerCount");
		out.addProperty("count", ServerLifecycleHooks.getCurrentServer().getPlayerCount());
		webSocket.sendText(out.toString(), true);
	}
}
