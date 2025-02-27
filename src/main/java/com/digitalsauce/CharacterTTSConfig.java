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
			keyName = "npcMaleVoiceName",
			name = "NPC Male Voice Name",
			description = "The name of the Azure voice to use for male NPC dialogue"
	)
	default String npcMaleVoiceName()
	{
		return "en-US-GuyNeural";
	}

	@ConfigItem(
			keyName = "npcFemaleVoiceName",
			name = "NPC Female Voice Name",
			description = "The name of the Azure voice to use for female NPC dialogue"
	)
	default String npcFemaleVoiceName()
	{
		return "en-US-AriaNeural";
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
			keyName = "npcMalePitch",
			name = "NPC Male Pitch",
			description = "The pitch control for male NPC TTS voice (e.g. default, +10%, -10%)"
	)
	default String npcMalePitch()
	{
		return "default";
	}

	@ConfigItem(
			keyName = "npcMaleVolume",
			name = "NPC Male Volume",
			description = "The volume control for male NPC TTS voice (e.g. default, loud, soft)"
	)
	default String npcMaleVolume()
	{
		return "default";
	}

	@ConfigItem(
			keyName = "npcFemalePitch",
			name = "NPC Female Pitch",
			description = "The pitch control for female NPC TTS voice (e.g. default, +10%, -10%)"
	)
	default String npcFemalePitch()
	{
		return "default";
	}

	@ConfigItem(
			keyName = "npcFemaleVolume",
			name = "NPC Female Volume",
			description = "The volume control for female NPC TTS voice (e.g. default, loud, soft)"
	)
	default String npcFemaleVolume()
	{
		return "default";
	}
}
