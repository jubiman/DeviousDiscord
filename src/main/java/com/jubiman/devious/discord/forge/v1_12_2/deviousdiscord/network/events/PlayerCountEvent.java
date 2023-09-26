package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.java_websocket.WebSocket;

public class PlayerCountEvent implements Event {
	@Override
	public void handle(WebSocket webSocket, JsonObject json) {
		JsonObject out = new JsonObject();
		out.addProperty("event", "playerCount");
		out.addProperty("count", FMLServerHandler.instance().getServer().getOnlinePlayerNames().length);
		webSocket.send(out.toString());
	}
}
