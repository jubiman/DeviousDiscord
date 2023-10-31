package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.WebSocketConnection;
import net.minecraftforge.fml.server.FMLServerHandler;

public class PlayerCountEvent implements Event {
	@Override
	public void handle(WebSocketConnection connection, JsonObject json) {
		JsonObject out = new JsonObject();
		out.addProperty("event", "playerCount");
		out.addProperty("count", FMLServerHandler.instance().getServer().getOnlinePlayerNames().length);
		connection.send(out.toString());
	}
}
