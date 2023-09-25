package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;


public interface Event {
	void handle(WebSocket webSocket, JsonObject json);
}
