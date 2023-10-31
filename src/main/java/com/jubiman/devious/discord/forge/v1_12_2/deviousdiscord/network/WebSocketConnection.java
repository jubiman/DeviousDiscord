package com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.DeviousDiscord;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.ModConfig;
import com.jubiman.devious.discord.forge.v1_12_2.deviousdiscord.network.events.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletionException;

/**
 * Handles the WebSocket connection to the Devious Socket.
 */
public class WebSocketConnection extends WebSocketClient {
	private static final Gson gson = new Gson();
	private static final HashMap<String, Event> events = new HashMap<>();
	private final TimerTask TIMER_TASK;

	static {
		events.put("identify", new IdentifyEvent());
		events.put("message", new MessageEvent());
		events.put("playerCount", new PlayerCountEvent());
		events.put("ticket", new TicketEvent());
	}

	public WebSocketConnection() {
		super(java.net.URI.create("ws://" + ModConfig.getHostname() + ":" + ModConfig.getPort() + "/WSSMessaging"));
		DeviousDiscord.LOGGER.debug("Connecting to Devious Socket on " + ModConfig.getHostname() + ":" + ModConfig.getPort());

		try {
			super.connect();
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to connect to Devious Socket.", e);
			new Timer().scheduleAtFixedRate(new TimerTask() {
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
			}, ModConfig.getReconnectInterval() * 1000L, ModConfig.getReconnectInterval() * 1000L);
			TIMER_TASK = null;
			return;
		}

		// Schedule a reconnect on a set interval, defined in the config (in seconds, so * 1000L ms)
		TIMER_TASK = new WebsocketTimerTask(this);
		new Timer().scheduleAtFixedRate(TIMER_TASK, ModConfig.getReconnectInterval() * 1000L, ModConfig.getReconnectInterval() * 1000L);

		DeviousDiscord.LOGGER.info("Successfully connected to Devious Socket and set up reconnect timer.");
	}

	/**
	 * Sends an event to the Devious Socket.
	 * @param json The event to send.
	 */
	private void sendJson(JsonObject json) {
		DeviousDiscord.LOGGER.debug("Sending event to Devious Socket: " + json);
		try {
			send(json.toString());
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to send event to Devious Socket. See debug logs for more info.");
			DeviousDiscord.LOGGER.debug("Failed to send event to Devious Socket.", e);
		}
		DeviousDiscord.LOGGER.info("Sent event to Devious Socket: " + json);
	}

	/**
	 * Sends a message to the Devious Socket.
	 *
	 * @param username The username of the player who sent the message.
	 * @param message  The message that was sent.
	 */
	public void sendMessage(String username, UUID uuid, String message) {
		JsonObject json = new JsonObject();
		json.addProperty("event", "message");
		json.addProperty("server", ModConfig.getIdentifier());
		json.addProperty("player", username);
		json.addProperty("channel", ChannelHandler.getChannel(uuid));
		json.addProperty("uuid", uuid.toString());
		json.addProperty("message", message);

		sendJson(json);
	}

	/**
	 * Called after an opening handshake has been performed and the given websocket is ready to be
	 * written on.
	 *
	 * @param handshakeData The handshake of the websocket instance
	 */
	@Override
	public void onOpen(ServerHandshake handshakeData) {
		DeviousDiscord.LOGGER.info("Devious Socket opened.");
	}

	@Override
	public void onMessage(String data) {
		DeviousDiscord.LOGGER.info("Received message from Devious Socket: " + data);

		try {
			JsonObject json = gson.fromJson(data, JsonObject.class);
			events.getOrDefault(json.get("event").getAsString(),
							(webSocket1, json1) -> DeviousDiscord.LOGGER
									.warn("Received unknown event from Devious Socket: " + json1.get("event").getAsString()))
					.handle(this, json);
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to parse JSON from Devious Socket.", e);
		}
	}

	@Override
	public void onClose(int statusCode, String reason, boolean remote) {
		DeviousDiscord.LOGGER.info("Devious Socket closed (closed by: " + (remote ? "remote" : "us")
				+ ") with status code " + statusCode + " and reason " + reason);
	}

	@Override
	public void onError(Exception error) {
		DeviousDiscord.LOGGER.error("Devious Socket errored", error);
	}

	/**
	 * Closes the WebSocket connection.
	 * 
	 * @param reason The reason for closing the connection.
	 */
	public void close(String reason) {
		try {
			this.closeConnection(1000, reason);
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to close Devious Socket.", e);
		}
	}

	/**
	 * Sends a player join/leave event to the Devious Socket.
	 * @param username The username of the player.
	 * @param joined Whether the player joined or left. True for joined, false for left.
	 */
	public void sendPlayerEvent(String username, UUID uuid, boolean joined) {
		JsonObject json = new JsonObject();
		json.addProperty("event", "playerState");
		json.addProperty("server", ModConfig.getIdentifier());
		json.addProperty("player", username);
		json.addProperty("uuid", uuid.toString());
		json.addProperty("joined", joined ? "joined" : "left");

		sendJson(json);
	}


	/**
	 * Sends a server state event to the Devious Socket.
	 * @param state The state of the server. (started, stopping)
	 */
	public void sendServerStateEvent(String state) {
		JsonObject json = new JsonObject();
		json.addProperty("event", "serverState");
		json.addProperty("server", ModConfig.getIdentifier());
		json.addProperty("state", state);

		sendJson(json);
	}

	@Override
	public void reconnect() {
		if (TIMER_TASK != null)
			TIMER_TASK.cancel();
		try {
			super.reconnect();
		} catch (Exception e) {
			DeviousDiscord.LOGGER.error("Failed to reconnect to Devious Socket.", e);
		}
	}

	private static class WebsocketTimerTask extends TimerTask {
		private final WebSocketConnection connection;

		public WebsocketTimerTask(WebSocketConnection connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			try {
				DeviousDiscord.LOGGER.debug("Checking if Devious Socket connection is closed...");
				if (connection.isClosed()) {
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
