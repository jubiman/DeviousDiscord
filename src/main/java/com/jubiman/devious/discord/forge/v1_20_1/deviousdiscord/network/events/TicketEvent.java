package com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network.WebSocketConnection;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TicketEvent implements Event {
	@Override
	public void handle(WebSocketConnection connection, JsonObject json) {
		Stream<JsonElement> s = StreamSupport.stream(json.get("names").getAsJsonArray().spliterator(), true);
		s.map(JsonElement::getAsString)
				.map(ServerLifecycleHooks.getCurrentServer().getPlayerList()::getPlayerByName)
				.filter(Objects::nonNull)
				.forEach(player -> player.displayClientMessage(ComponentUtils.fromMessage(() -> json.get("message").getAsString()), false));
	}
}
