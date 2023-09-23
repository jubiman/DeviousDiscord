package com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.Config;
import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.DeviousDiscord;
import net.minecraft.network.chat.Component;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Handles the WebSocket connection to the Devious Socket.
 */
public class WebSocketConnection implements WebSocket.Listener {
	private final WebSocket webSocket;

	public WebSocketConnection() {
		DeviousDiscord.LOGGER.debug("Connecting to Devious Socket on " + Config.getHostname() + ":" + Config.getPort());

		CompletableFuture<WebSocket> sock = HttpClient.newHttpClient().newWebSocketBuilder()
				.buildAsync(java.net.URI.create("ws://" + Config.getHostname() + ":" + Config.getPort() + "/WSSMessaging"), this);
		webSocket = sock.join();

		// Create JSON object to send
		JsonObject json = new JsonObject();
		json.addProperty("event", "identify");
		json.addProperty("identifier", Config.getIdentifier());
		webSocket.sendText(json.toString(), true);
	}

	/**
	 * Sends a chat message to the Devious Socket.
	 * @param username The username of the player who sent the message.
	 * @param message The message to send.
	 */
	public void sendMessage(String username, Component message) {
		JsonObject json = new JsonObject();
		json.addProperty("event", "message");
		json.addProperty("server", Config.getIdentifier());
		json.addProperty("player", username);
		json.addProperty("message", message.getString());

		DeviousDiscord.LOGGER.debug("Sending message to Devious Socket: " + json.toString());
		CompletableFuture<WebSocket> future = webSocket.sendText(json.toString(), true);
		try {
			future.join();
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to send message to Devious Socket.", e);
		}
		DeviousDiscord.LOGGER.debug("Sent message to Devious Socket: " + json);
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		DeviousDiscord.LOGGER.debug("Received message from Devious Socket: " + data);
		webSocket.request(1);
		if (!last) {
			DeviousDiscord.LOGGER.debug("Received partial message from Devious Socket: " + data);
			return null;
		}


		JsonObject json = new Gson().fromJson(data.toString(), JsonObject.class);
		//try (JsonReader reader = Json.createReaderFactory(null).createReader(new StringReader(data.toString()))) {
		if (json.has("event")) {
			switch (json.get("event").getAsString()) {
				case "message" -> {
					if (json.has("channel") && json.get("channel").getAsString().equals("global")) {
						ChannelHandler.sendMessageToGlobalChannel(Config.getGlobalMessageFormat().replaceAll("(?<!\\\\)%s", Config.getIdentifier())
								.replaceAll("(?<!\\\\)%u", json.get("username").getAsString())
								.replaceAll("(?<!\\\\)%m", json.get("message").getAsString()));
					} else if (json.has("channel") && json.get("channel").getAsString().equals("server")) {
						ChannelHandler.sendMessageToServerChannel(Config.getServerMessageFormat().replaceAll("(?<!\\\\)%s", Config.getIdentifier())
								.replaceAll("(?<!\\\\)%u", json.get("username").getAsString())
								.replaceAll("(?<!\\\\)%m", json.get("message").getAsString()));
					} else {
						DeviousDiscord.LOGGER.debug("Received unknown message from Devious Socket: " + json.get("message").getAsString());
					}
				}
				default -> DeviousDiscord.LOGGER.debug("Received unknown event from Devious Socket: " + json.get("event").getAsString());
			}
		} else {
			DeviousDiscord.LOGGER.error("Received unknown message from Devious Socket: " + data);
		}
		return null;
	}

	@Override
	public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
		webSocket.request(1);
		DeviousDiscord.LOGGER.debug("Received binary message from Devious Socket: " + data);
		return null;
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		DeviousDiscord.LOGGER.info("Devious Socket closed with status code " + statusCode + " and reason " + reason);
		return null;
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		DeviousDiscord.LOGGER.error("Devious Socket errored", error);
	}

	/**
	 * Closes the WebSocket connection.
	 * @param reason The reason for closing the connection.
	 */
	public void close(String reason) {
		CompletableFuture<WebSocket> future = webSocket.sendClose(WebSocket.NORMAL_CLOSURE, reason);
		try {
			future.join();
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to close Devious Socket.", e);
		}
	}
}
