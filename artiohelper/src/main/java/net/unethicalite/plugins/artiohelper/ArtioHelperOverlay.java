package net.unethicalite.plugins.artiohelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class ArtioHelperOverlay extends Overlay
{
	private final Client client;
	private final ArtioHelperPlugin plugin;
	private final SpriteManager spriteManager;

	@Inject
	private ArtioHelperOverlay(Client client, ArtioHelperPlugin plugin, final SpriteManager sprite)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.plugin = plugin;
		this.spriteManager = sprite;
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (plugin.tickCounter && plugin.bear != null && plugin.attackLength != -1)
		{
			final int ticks = plugin.attackLength - client.getTickCount();
			if (ticks < 0)
			{
				return null;
			}
			Color color;
			switch (ticks)
			{
				case 1:
					color = Color.RED;
					break;
				case 2:
					color = Color.YELLOW;
					break;
				default:
					color = Color.WHITE;
					break;
			}

			final String text = String.valueOf(ticks);
			g.setFont(new Font("Arial", Font.BOLD, 32));
			OverlayUtil.renderTextLocation(g, plugin.bear.getCanvasTextLocation(g, text, -20), text, color);

			if (plugin.needMage)
			{
				OverlayUtil.renderActorOverlayImage(g, plugin.bear, spriteManager.getSprite(SpriteID.PRAYER_PROTECT_FROM_MAGIC, 0), Color.WHITE, 50);
			}
		}

		return null;
	}
}