package com.digitalsauce;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("charactertts")
public interface CharacterTTSConfig extends Config {
	@ConfigItem(
			keyName = "azureApiKey",
			name = "Azure API Key",
			description = "Your Azure API Key for TTS",
			position = 1
	)
	default String azureApiKey() {
		return "";
	}

	@ConfigItem(
			keyName = "azureRegion",
			name = "Azure Region",
			description = "Your Azure region (e.g. westus)",
			position = 2
	)
	default String azureRegion() {
		return "";
	}

	@ConfigItem(
			keyName = "greeting",
			name = "Greeting",
			description = "The greeting text to be spoken on login",
			position = 3
	)
	default String greeting() {
		return "Welcome back to the game!";
	}

	@ConfigItem(
			keyName = "azureVoiceName",
			name = "Player Voice Name",
			description = "The name of the Azure voice to use for player dialogue",
			position = 4
	)
	default String azureVoiceName() {
		return "en-US-AriaNeural";
	}

	@Range(min = -50, max = 50)
	@ConfigItem(
			keyName = "pitch",
			name = "Player Pitch",
			description = "The pitch control for player TTS voice (e.g. default, +10%, -10%)",
			position = 5
	)
	default String pitch() {
		return "default";
	}

	@Range(min = 0, max = 200)
	@ConfigItem(
			keyName = "volume",
			name = "Player Volume",
			description = "The volume control for player TTS voice (e.g. default, loud, soft)",
			position = 6
	)
	default String volume() {
		return "default";
	}

	@ConfigItem(
			keyName = "npcMaleVoiceName",
			name = "NPC Male Voice Name",
			description = "The name of the Azure voice to use for male NPC dialogue",
			position = 7
	)
	default String npcMaleVoiceName() {
		return "en-US-GuyNeural";
	}

	@Range(min = -50, max = 50)
	@ConfigItem(
			keyName = "npcMalePitch",
			name = "NPC Male Pitch",
			description = "The pitch control for male NPC TTS voice (e.g. default, +10%, -10%)",
			position = 8
	)
	default String npcMalePitch() {
		return "default";
	}

	@Range(min = 0, max = 200)
	@ConfigItem(
			keyName = "npcMaleVolume",
			name = "NPC Male Volume",
			description = "The volume control for male NPC TTS voice (e.g. default, loud, soft)",
			position = 9
	)
	default String npcMaleVolume() {
		return "default";
	}

	@ConfigItem(
			keyName = "npcFemaleVoiceName",
			name = "NPC Female Voice Name",
			description = "The name of the Azure voice to use for female NPC dialogue",
			position = 10
	)
	default String npcFemaleVoiceName() {
		return "en-US-AriaNeural";
	}

	@Range(min = -50, max = 50)
	@ConfigItem(
			keyName = "npcFemalePitch",
			name = "NPC Female Pitch",
			description = "The pitch control for female NPC TTS voice (e.g. default, +10%, -10%)",
			position = 11
	)
	default String npcFemalePitch() {
		return "default";
	}

	@Range(min = 0, max = 200)
	@ConfigItem(
			keyName = "npcFemaleVolume",
			name = "NPC Female Volume",
			description = "The volume control for female NPC TTS voice (e.g. default, loud, soft)",
			position = 12
	)
	default String npcFemaleVolume() {
		return "default";
	}

	@ConfigItem(
			keyName = "blockFloatingTextWhileDialogue",
			name = "Block Floating Text While Dialogue",
			description = "When enabled, floating text TTS will be blocked while dialogue is active",
			position = 13
	)
	default boolean blockFloatingTextWhileDialogue() {
		return true;
	}

	@Range(min = 1000, max = 10000)
	@ConfigItem(
			keyName = "playbackCooldown",
			name = "Playback Cooldown (ms)",
			description = "Cooldown time between duplicate TTS playback in milliseconds",
			position = 14
	)
	default int playbackCooldown() {
		return 5000;
	}

	@Range(min = 1, max = 100)
	@ConfigItem(
			keyName = "maxDistance",
			name = "Max Distance",
			description = "Maximum distance (in tiles) for floating text attenuation",
			position = 15
	)
	default int maxDistance() {
		return 8;
	}

	@Range(min = 0, max = 1)
	@ConfigItem(
			keyName = "minVolumeRatio",
			name = "Min Volume Ratio",
			description = "Minimum volume ratio for floating text attenuation",
			position = 16
	)
	default double minVolumeRatio() {
		return 0.11;
	}

	@Range(min = 0, max = 1)
	@ConfigItem(
			keyName = "maxVolumeRatio",
			name = "Max Volume Ratio",
			description = "Maximum volume ratio for floating text attenuation",
			position = 17
	)
	default double maxVolumeRatio() {
		return 0.66;
	}
}