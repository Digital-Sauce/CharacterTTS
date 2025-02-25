package com.digitalsauce;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("charactertts")
public interface CharacterTTSConfig extends Config
{
	@ConfigItem(
			keyName = "azureApiKey",
			name = "Azure API Key",
			description = "Your Azure API Key for TTS"
	)
	default String azureApiKey()
	{
		return "";
	}

	@ConfigItem(
			keyName = "azureRegion",
			name = "Azure Region",
			description = "Your Azure region (e.g. westus)"
	)
	default String azureRegion()
	{
		return "";
	}

	@ConfigItem(
			keyName = "azureVoiceName",
			name = "Player Voice Name",
			description = "The name of the Azure voice to use for player dialogue"
	)
	default String azureVoiceName()
	{
		return "en-US-AriaNeural";
	}

	@ConfigItem(
			keyName = "npcVoiceName",
			name = "NPC Voice Name",
			description = "The name of the Azure voice to use for NPC dialogue"
	)
	default String npcVoiceName()
	{
		return "en-US-GuyNeural";
	}

	@ConfigItem(
			keyName = "greeting",
			name = "Greeting",
			description = "The greeting text to be spoken on login"
	)
	default String greeting()
	{
		return "Welcome to the game!";
	}

	@ConfigItem(
			keyName = "pitch",
			name = "Player Pitch",
			description = "The pitch control for player TTS voice (e.g. default, +10%, -10%)"
	)
	default String pitch()
	{
		return "default";
	}

	@ConfigItem(
			keyName = "volume",
			name = "Player Volume",
			description = "The volume control for player TTS voice (e.g. default, loud, soft)"
	)
	default String volume()
	{
		return "default";
	}

	@ConfigItem(
			keyName = "npcPitch",
			name = "NPC Pitch",
			description = "The pitch control for NPC TTS voice (e.g. default, +10%, -10%)"
	)
	default String npcPitch()
	{
		return "default";
	}

	@ConfigItem(
			keyName = "npcVolume",
			name = "NPC Volume",
			description = "The volume control for NPC TTS voice (e.g. default, loud, soft)"
	)
	default String npcVolume()
	{
		return "default";
	}
}
