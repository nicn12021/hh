package com.robloxify.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Base class for every Robloxify screen: dark backdrop, header bar and tab buttons. */
public abstract class RobloxScreen extends Screen {
	protected static final int HEADER_HEIGHT = 28;
	protected static final int TAB_WIDTH = 72;
	protected static final int TAB_HEIGHT = 18;

	protected RobloxScreen(Component title) {
		super(title);
	}

	@Override
	protected void init() {
		super.init();
		int x = 4;
		for (RobloxTab tab : RobloxTab.values()) {
			if (tab == currentTab()) {
				x += TAB_WIDTH + 2;
				continue;
			}
			RobloxTab target = tab;
			addRenderableWidget(Button.builder(Component.literal(tab.label()), button -> openTab(target))
					.bounds(x, HEADER_HEIGHT - TAB_HEIGHT - 3, TAB_WIDTH, TAB_HEIGHT)
					.build());
			x += TAB_WIDTH + 2;
		}
	}

	protected void openTab(RobloxTab tab) {
		Screen screen = tab.create();
		if (screen != null) {
			this.minecraft.gui.setScreen(screen);
		}
	}

	protected abstract RobloxTab currentTab();

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, RobloxTheme.BACKDROP);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		// Panel behind the widgets.
		graphics.fill(4, HEADER_HEIGHT + 2, this.width - 4, this.height - 4, RobloxTheme.PANEL);
		graphics.fill(0, 0, this.width, HEADER_HEIGHT, RobloxTheme.HEADER);
		graphics.fill(0, HEADER_HEIGHT - 2, this.width, HEADER_HEIGHT, RobloxTheme.ACCENT);
		graphics.text(this.font, this.title, 8, 8, RobloxTheme.TEXT);
		graphics.text(this.font, "\u25C8 " + com.robloxify.client.ClientRobloxState.formatNumber(
				com.robloxify.client.ClientRobloxState.robux()), this.width - 90, 8, RobloxTheme.GOLD);

		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		// Highlight the active tab on top of the widgets.
		int x = 4;
		for (RobloxTab tab : RobloxTab.values()) {
			if (tab == currentTab()) {
				int y = HEADER_HEIGHT - TAB_HEIGHT - 3;
				graphics.fill(x, y, x + TAB_WIDTH, y + TAB_HEIGHT, RobloxTheme.ACCENT_DARK);
				graphics.centeredText(this.font, tab.label(), x + TAB_WIDTH / 2, y + 5, RobloxTheme.TEXT);
			}
			x += TAB_WIDTH + 2;
		}
	}
}
