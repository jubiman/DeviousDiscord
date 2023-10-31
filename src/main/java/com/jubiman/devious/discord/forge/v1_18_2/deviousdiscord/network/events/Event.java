package com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events;

import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.WebSocketConnection;

public interface Event {
	void handle(WebSocketConnection connection, JsonObject json);
}
