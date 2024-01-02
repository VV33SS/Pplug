package net.unethicalite.plugins.artiohelper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.SpriteID;
import net.runelite.api.Varbits;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Artio & Callisto Helper",
	description = "Pray & stepback helper for Artio and Callisto",
	tags = {"pvm", "artio", "callisto"},
	enabledByDefault = false
)
public class ArtioHelperPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "artiohelper";

	@Inject
	private Client client;

	@Inject
	private ArtioHelperConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ArtioHelperOverlay overlay;

	@Inject
	private SpriteManager spriteManager;

	//config
	public boolean tickCounter = false;

	private static final int ARTIO_REGION = 6835;
	private static final int ARTIO_ID = 11992;
	private static final int ARTIO_MELEE_ID = 10012;
	private static final int ARTIO_RANGE_ID = 10013;
	private static final int ARTIO_MAGIC_ID = 10014;
	private static final int ARTIO_TRAP_ID = 10015;
	private static final int ARTIO_ROAR_ID = 10016;
	private static final int ARTIO_DEATH_ID = 10017;
	private static final int ARTIO_MELEE_TICKS = 6;
	private static final int ARTIO_RANGE_TICKS = 5;
	private static final int ARTIO_MAGIC_TICKS = 8;
	private static final int ARTIO_TRAP_TICKS = 6;
	private static final int ARTIO_ROAR_TICKS = 6;
	private static final int CALLISTO_ID = 6609;

	public int attackLength = 0;
	public int nextAttack = -1;
	public boolean needMage = false;
	public NPC bear = null;
	public boolean inCave = false;

	@Provides
	ArtioHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ArtioHelperConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		updateConfig();
		setGameStateParams(client.getGameState());
		addOverlays();
	}

	@Override
	protected void shutDown() throws Exception
	{
		removeOverlays();
	}

	private void addOverlays()
	{
		overlayManager.add(overlay);
	}

	private void removeOverlays()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		updateConfig();
	}

	private void setGameStateParams(GameState state)
	{
		switch (state)
		{
			case HOPPING:
			case LOGIN_SCREEN:
			case LOADING:
				break;
			case LOGGED_IN:
				bear = null;
				break;
		}
	}

	private void updateConfig()
	{
		tickCounter = config.tickCounter();
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		final int id = event.getNpc().getId();
		if (id == ARTIO_ID || id == CALLISTO_ID)
		{
			bear = event.getNpc();
			if (bear.getAnimation() == ARTIO_ROAR_ID)
			{
				attackLength = client.getTickCount() + ARTIO_ROAR_TICKS;
				nextAttack = -1;
			}
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event)
	{
		if (event.getActor() == bear)
		{
			bear = null;
		}
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() != bear)
		{
			return;
		}

		final int animation = event.getActor().getAnimation();
		final int ticks = client.getTickCount();

		if (animation == ARTIO_MELEE_ID)
		{
			attackLength = ticks + ARTIO_MELEE_TICKS;
			nextAttack = -1;
		}
		else if (animation == ARTIO_RANGE_ID)
		{
			attackLength = ticks + ARTIO_RANGE_TICKS;
			nextAttack = -1;
		}
		else if (animation == ARTIO_MAGIC_ID)
		{
			attackLength = ticks + ARTIO_MAGIC_TICKS;
			nextAttack = SpriteID.PRAYER_PROTECT_FROM_MAGIC;
			if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MAGIC) == 0)
			{
				needMage = true;
			}
		}
		else if (animation == ARTIO_TRAP_ID)
		{
			attackLength = ticks + ARTIO_TRAP_TICKS;
			nextAttack = -1;
		}
		else if (animation == ARTIO_ROAR_ID)
		{
			attackLength = ticks + ARTIO_ROAR_TICKS;
			nextAttack = -1;
		}
		else
		{
			nextAttack = -1;
		}

		needMage = false;
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (bear == null)
		{
			return;
		}

		final int ticksLeft = this.attackLength - client.getTickCount();

		if (ticksLeft == 1 && client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) == 0)
		{
			needMage = false;
		}
	}
}