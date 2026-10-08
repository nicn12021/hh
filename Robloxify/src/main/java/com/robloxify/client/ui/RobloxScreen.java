package com.robloxify.client.ui;

import com.robloxify.client.ClientRobloxState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Base class for every Robloxify screen: backdrop, header bar and left navigation rail. */
public abstract class RobloxScreen extends Screen {
	protected static final int HEADER_HEIGHT = 28;
	protected static final int SIDEBAR_X = 6;
	protected static final int SIDEBAR_Y = 38;
	protected static final int SIDEBAR_WIDTH = 88;
	protected static final int NAV_HEIGHT = 20;
	protected static final int NAV_GAP = 3;
	protected static final int CONTENT_X = SIDEBAR_X + SIDEBAR_WIDTH + 10;
	protected static final int CONTENT_Y = 38;

	protected RobloxScreen(Component title) {
		super(title);
	}

	@Override
	protected void init() {
		super.init();
		int y = SIDEBAR_Y;
		for (RobloxTab tab : RobloxTab.values()) {
			if (tab == currentTab()) {
				y += NAV_HEIGHT + NAV_GAP;
				continue;
			}
			RobloxTab target = tab;
			addRenderableWidget(new RobloxButton(SIDEBAR_X, y, SIDEBAR_WIDTH, NAV_HEIGHT,
					Component.literal(tab.icon() + "  " + tab.label()), () -> open(target))
					.leftAligned().accent(RobloxTheme.ACCENT));
			y += NAV_HEIGHT + NAV_GAP;
		}
	}

	protected void open(RobloxTab tab) {
		Screen screen = tab.create();
		if (screen != null) {
			this.minecraft.gui.setScreen(screen);
		}
	}

	protected abstract RobloxTab currentTab();

	protected int contentWidth() {
		return this.width - CONTENT_X - 10;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, RobloxTheme.BACKDROP_TOP);
		graphics.fill(0, this.height / 2, this.width, this.height, RobloxTheme.BACKDROP_BOTTOM);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int navHeight = RobloxTab.values().length * (NAV_HEIGHT + NAV_GAP);
		RobloxUi.panel(graphics, SIDEBAR_X, SIDEBAR_Y, SIDEBAR_WIDTH, navHeight, RobloxTheme.SIDEBAR);

		int activeY = SIDEBAR_Y + currentTab().ordinal() * (NAV_HEIGHT + NAV_GAP);
		graphics.fill(SIDEBAR_X, activeY, SIDEBAR_X + 3, activeY + NAV_HEIGHT, RobloxTheme.ACCENT);
		graphics.text(this.font, currentTab().icon() + "  " + currentTab().label(),
				SIDEBAR_X + 11, activeY + 6, RobloxTheme.ACCENT_LIGHT);

		graphics.fill(0, 0, this.width, HEADER_HEIGHT, RobloxTheme.HEADER);
		graphics.fill(0, HEADER_HEIGHT - 2, this.width, HEADER_HEIGHT, RobloxTheme.ACCENT);
		graphics.text(this.font, "ROBLOXIFY", 10, 10, RobloxTheme.TEXT);
		graphics.text(this.font, "/ " + this.title.getString(), 82, 10, RobloxTheme.TEXT_DIM);
		RobloxUi.robuxChip(graphics, this.font, this.width - 8, 7, ClientRobloxState.robux());

		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}
}
