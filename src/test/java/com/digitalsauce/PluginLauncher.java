package com.digitalsauce;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginLauncher
{
	public static void main(String[] args) throws Exception
	{
		//ExternalPluginManager.loadBuiltin(ExamplePlugin.class);
		ExternalPluginManager.loadBuiltin(CharacterTTSPlugin.class);
		RuneLite.main(args);
	}
}