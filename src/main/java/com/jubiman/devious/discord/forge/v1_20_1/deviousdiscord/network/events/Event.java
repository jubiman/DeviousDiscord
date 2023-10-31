package com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.WebSocketConnection;

import java.net.http.WebSocket;

public interface Event {
	void handle(WebSocketConnection connection, JsonObject json);
}
