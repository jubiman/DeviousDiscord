package com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.network;

import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.Config;
import com.jubiman.devious.discord.forge.v1_20_1.deviousdiscord.DeviousDiscord;
import net.minecraft.network.chat.Component;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
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
		JsonObject json = Json.createObjectBuilder()
				.add("event", "identify")
				.add("identifier", Config.getIdentifier())
				.build();

		webSocket.sendText(json.toString(), true);
	}

	/**
	 * Sends a chat message to the Devious Socket.
	 * @param username The username of the player who sent the message.
	 * @param message The message to send.
	 */
	public void sendMessage(String username, Component message) {
		JsonObject json = Json.createObjectBuilder()
				.add("event", "message")
				.add("server", Config.getIdentifier())
				.add("player", username)
				.add("message", message.getString())
				.build();

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


		try (JsonReader reader = Json.createReaderFactory(null).createReader(new StringReader(data.toString()))) {
			JsonObject json = reader.readObject();
			if (json.containsKey("event")) {
				switch (json.getString("event")) {
					case "message" -> {
						if (json.containsKey("channel") && json.getString("channel").equals("global")) {
							ChannelHandler.sendMessageToGlobalChannel(Config.getGlobalMessageFormat().replaceAll("(?<!\\\\)%s", Config.getIdentifier())
									.replaceAll("(?<!\\\\)%u", json.getString("username"))
									.replaceAll("(?<!\\\\)%m", json.getString("message")));
						} else if (json.containsKey("channel") && json.getString("channel").equals("server")) {
							ChannelHandler.sendMessageToServerChannel(Config.getServerMessageFormat().replaceAll("(?<!\\\\)%s", Config.getIdentifier())
									.replaceAll("(?<!\\\\)%u", json.getString("username"))
									.replaceAll("(?<!\\\\)%m", json.getString("message")));
						} else {
							DeviousDiscord.LOGGER.debug("Received unknown message from Devious Socket: " + json.getString("message"));
						}
					}
					default ->
							DeviousDiscord.LOGGER.debug("Received unknown event from Devious Socket: " + json.getString("event"));
				}
			} else {
				DeviousDiscord.LOGGER.error("Received unknown message from Devious Socket: " + data);
			}
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
