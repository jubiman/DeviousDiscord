package com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.Config;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.DeviousDiscord;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events.Event;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events.IdentifyEvent;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events.MessageEvent;
import com.jubiman.devious.discord.forge.v1_18_2.deviousdiscord.network.events.PlayerCountEvent;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * Handles the WebSocket connection to the Devious Socket.
 */
public class WebSocketConnection implements WebSocket.Listener {

	private static final Gson gson = new Gson();
	private WebSocket webSocket;
	private static final HashMap<String, Event> events = new HashMap<>();
	private String buffer = "";
	private final TimerTask TIMER_TASK;

	static {
		events.put("identify", new IdentifyEvent());
		events.put("message", new MessageEvent());
		events.put("playerCount", new PlayerCountEvent());
	}

	public WebSocketConnection() {
		DeviousDiscord.LOGGER.debug("Connecting to Devious Socket on " + Config.getHostname() + ":" + Config.getPort());

		CompletableFuture<WebSocket> sock = HttpClient.newHttpClient().newWebSocketBuilder()
				.buildAsync(java.net.URI.create("ws://" + Config.getHostname() + ":" + Config.getPort() + "/WSSMessaging"), this);

		try {
			webSocket = sock.join();
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to connect to Devious Socket.", e);
			new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						DeviousDiscord.LOGGER.info("Devious Socket connection closed, reconnecting...");
						reconnect();
						// Stop the initial timer task
						cancel();
					} catch (CompletionException e) {
						DeviousDiscord.LOGGER.error("Failed to reconnect to Devious Socket. Probably offline, check debug logs for more info.");
						DeviousDiscord.LOGGER.debug("Failed to reconnect to Devious Socket.", e);
					}
				}
			}, Config.getReconnectInterval() * 1000L, Config.getReconnectInterval() * 1000L);
			TIMER_TASK = null;
			return;
		}

		// Schedule a reconnect on a set interval, defined in the config (in seconds, so * 1000L ms)
		TIMER_TASK = new WebsocketTimerTask(webSocket, this);
		new java.util.Timer().scheduleAtFixedRate(TIMER_TASK, Config.getReconnectInterval() * 1000L, Config.getReconnectInterval() * 1000L);
	}


	/**
	 * Sends an event to the Devious Socket.
	 * @param json The event to send.
	 */
	private void sendJson(JsonObject json) {
		DeviousDiscord.LOGGER.debug("Sending event to Devious Socket: " + json);
		try {
			webSocket.sendText(json.toString(), true).join();
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to send event to Devious Socket. See debug logs for more info.");
			DeviousDiscord.LOGGER.debug("Failed to send event to Devious Socket.", e);
		}
		DeviousDiscord.LOGGER.info("Sent event to Devious Socket: " + json);
	}

	/**
	 * Sends a chat message to the Devious Socket.
	 * @param username The username of the player who sent the message.
	 * @param message The message to send.
	 */
	public void sendMessage(String username, UUID uuid, String message) {
		JsonObject json = new JsonObject();
		json.addProperty("event", "message");
		json.addProperty("server", Config.getIdentifier());
		json.addProperty("channel", ChannelHandler.getChannel(uuid));
		json.addProperty("player", username);
		json.addProperty("uuid", uuid.toString());
		json.addProperty("message", message);

		sendJson(json);
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		webSocket.request(1);
		buffer += data;
		if (!last) {
			DeviousDiscord.LOGGER.debug("Received partial message from Devious Socket: " + data);
			return null;
		}
		DeviousDiscord.LOGGER.info("Received message from Devious Socket: " + buffer);

		try {
			JsonObject json = gson.fromJson(buffer, JsonObject.class);
			events.getOrDefault(json.get("event").getAsString(),
							(webSocket1, json1) -> DeviousDiscord.LOGGER
									.warn("Received unknown event from Devious Socket: " + json1.get("event").getAsString())
			).handle(webSocket, json);
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to parse JSON from Devious Socket.", e);
			return null;
		}
		// Reset buffer
		buffer = "";
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
		this.webSocket = null;
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
		try {
			webSocket.sendClose(WebSocket.NORMAL_CLOSURE, reason).join();
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to close Devious Socket.", e);
		}
	}

	/**
	 * Reconnects to the Devious Socket.
	 */
	public void reconnect() {
		TIMER_TASK.cancel();
		CompletableFuture<WebSocket> sock = HttpClient.newHttpClient().newWebSocketBuilder()
				.buildAsync(
						java.net.URI.create("ws://" + Config.getHostname() + ":" + Config.getPort() + "/WSSMessaging"),
						this);

		try {
			webSocket = sock.join();
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to connect to Devious Socket.", e);
		}
	}

	/**
	 * Sends a player join/leave event to the Devious Socket.
	 * @param username The username of the player.
	 * @param state Whether the player joined or left.
	 */
	public void sendPlayerEvent(String username, UUID uuid, String state) {
		JsonObject json = new JsonObject();
		json.addProperty("event", "playerState");
		json.addProperty("server", Config.getIdentifier());
		json.addProperty("player", username);
		json.addProperty("uuid", uuid.toString());
		json.addProperty("joined", state);

		sendJson(json);
	}

	/**
	 * Sends a server state event to the Devious Socket.
	 * @param state The state of the server. (started, stopping)
	 */
	public void sendServerStateEvent(String state) {
		JsonObject json = new JsonObject();
		json.addProperty("event", "serverState");
		json.addProperty("server", Config.getIdentifier());
		json.addProperty("state", state);

		sendJson(json);
	}

	private static class WebsocketTimerTask extends TimerTask {
		private final WebSocket webSocket;
		private final WebSocketConnection connection;

		public WebsocketTimerTask(WebSocket webSocket, WebSocketConnection connection) {
			this.webSocket = webSocket;
			this.connection = connection;
		}

		@Override
		public void run() {
			try {
				DeviousDiscord.LOGGER.debug("Checking if Devious Socket connection is closed...");
				if (webSocket == null || webSocket.isInputClosed() || webSocket.isOutputClosed()) {
					DeviousDiscord.LOGGER.info("Devious Socket connection closed, reconnecting...");
					connection.reconnect();
				}
			} catch (CompletionException e) {
				DeviousDiscord.LOGGER.error("Failed to reconnect to Devious Socket. Probably offline, check debug logs for more info.");
				DeviousDiscord.LOGGER.debug("Failed to reconnect to Devious Socket.", e);
			}
		}

	}
}
