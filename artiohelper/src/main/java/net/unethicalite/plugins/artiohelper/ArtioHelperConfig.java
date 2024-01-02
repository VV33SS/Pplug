package net.unethicalite.plugins.artiohelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("artiohelper")
public interface ArtioHelperConfig extends Config
{
	@ConfigItem(
		keyName = "tickCounter",
		name = "Tick Counter",
		description = "Show ticks left til attack",
		position = 0
	)
	default boolean tickCounter()
	{
		return true;
	}
}