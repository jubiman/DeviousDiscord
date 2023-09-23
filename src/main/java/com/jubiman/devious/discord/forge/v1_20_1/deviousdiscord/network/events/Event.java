package com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.events;

import com.google.gson.JsonObject;

import java.net.http.WebSocket;

public interface Event {
	void handle(WebSocket webSocket, JsonObject json);
}
