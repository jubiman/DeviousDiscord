package com.jubiman.devious.discord.forge.v1_19_2.deviousdiscord.network;

/**
 * Defined discord channel types. These channels apply only to the discord channel. Minecraft's messages are always sent to the player.
 * <ul>
 * <li>GLOBAL receives messages from both GLOBAL and SERVER channels.</li>
 * <li>SERVER receives messages from SERVER channel only.</li>
 * <li>NONE receives no messages.</li>
 * </ul>
 */
public enum ChannelType {
	GLOBAL,
	SERVER,
	NONE
}
