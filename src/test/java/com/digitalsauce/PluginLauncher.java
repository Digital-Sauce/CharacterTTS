package com.digitalsauce;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginLauncher
{
	public static void main(String[] args) throws Exception
	{
		// Log a welcome message
		System.out.println("Welcome");

		ExternalPluginManager.loadBuiltin(CharacterTTSPlugin.class);
		RuneLite.main(args);
	}
}
