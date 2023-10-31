package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.WebSocketConnection;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.java_websocket.WebSocket;

import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TicketEvent implements Event {
	@Override
	public void handle(WebSocketConnection connection, JsonObject json) {
		Stream<JsonElement> s = StreamSupport.stream(json.get("names").getAsJsonArray().spliterator(), true);
		s.map(JsonElement::getAsString)
				.map(FMLServerHandler.instance().getServer().getPlayerList()::getPlayerByUsername)
				.filter(Objects::nonNull)
				.forEach(player -> player.sendMessage(new TextComponentString(json.get("message").getAsString())));
	}
}

