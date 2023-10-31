package com.jubiman.devious.discord.forge.v1_19_2.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_19_2.deviousdiscord.Config;
import com.jubiman.devious.discord.forge.v1_19_2.deviousdiscord.network.WebSocketConnection;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PlayerCountEvent implements Event {
	@Override
	public void handle(WebSocketConnection connection, JsonObject json) {
		JsonObject out = new JsonObject();
		out.addProperty("event", "playerCount");
		out.addProperty("count", ServerLifecycleHooks.getCurrentServer().getPlayerCount());
		out.addProperty("server", Config.getIdentifier());
		connection.sendJson(out);
	}
}
