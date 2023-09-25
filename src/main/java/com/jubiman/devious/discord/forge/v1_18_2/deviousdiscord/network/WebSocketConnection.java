package com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.Config;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.DeviousDiscord;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events.Event;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events.IdentifyEvent;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events.MessageEvent;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Handles the WebSocket connection to the Devious Socket.
 */
public class WebSocketConnection implements WebSocket.Listener {

	private static final Gson gson = new Gson();
	private WebSocket webSocket;
	private static final HashMap<String, Event> events = new HashMap<>();

	static {
		events.put("identify",
				new IdentifyEvent());
		events.put("message",
				new MessageEvent());
	}

	public WebSocketConnection() {
		DeviousDiscord.LOGGER.debug("Connecting to Devious Socket on " + Config.getHostname() + ":" + Config.getPort());

		CompletableFuture<WebSocket> sock = HttpClient.newHttpClient().newWebSocketBuilder()
				.buildAsync(java.net.URI.create("ws://" + Config.getHostname() + ":" + Config.getPort() + "/WSSMessaging"), this);
		webSocket = sock.join();
	}

	/**
	 * Sends a chat message to the Devious Socket.
	 * @param username The username of the player who sent the message.
	 * @param message The message to send.
	 */
	public void sendMessage(String username, String message) {
		JsonObject json = new JsonObject();
		json.addProperty("event", "message");
		json.addProperty("server", Config.getIdentifier());
		json.addProperty("player", username);
		json.addProperty("message", message);

		DeviousDiscord.LOGGER.debug("Sending message to Devious Socket: " + json);
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

		com.google.gson.JsonObject json = gson.fromJson(data.toString(), com.google.gson.JsonObject.class);
		if (json.has("event")) {
			events.getOrDefault(json.get("event").getAsString(),
							(webSocket1, json1) -> DeviousDiscord.LOGGER
									.warn("Received unknown event from Devious Socket: " + json1.get("event").getAsString()))
					.handle(webSocket, json);
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

	/**
	 * Reconnects to the Devious Socket.
	 */
	public void reconnect() {
		CompletableFuture<WebSocket> sock = HttpClient.newHttpClient().newWebSocketBuilder()
				.buildAsync(
						java.net.URI.create("ws://" + Config.getHostname() + ":" + Config.getPort() + "/WSSMessaging"),
						this);
		webSocket = sock.join();
	}

	/**
	 * Sends a player join/leave event to the Devious Socket.
	 * @param username The username of the player.
	 * @param joined Whether the player joined or left. True for joined, false for left.
	 */
	public void sendPlayerEvent(String username, boolean joined) {
		JsonObject json = new JsonObject();
		json.addProperty("event", "playerState");
		json.addProperty("server", Config.getIdentifier());
		json.addProperty("player", username);
		json.addProperty("joined", joined ? "joined" : "left");

		DeviousDiscord.LOGGER.debug("Sending playerState event to Devious Socket: " + json);
		try {
			webSocket.sendText(json.toString(), true).join();
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to send player event to Devious Socket.", e);
		}
	}
}
