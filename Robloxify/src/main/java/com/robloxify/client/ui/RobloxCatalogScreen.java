package com.robloxify.client.ui;

import com.robloxify.avatar.Cosmetic;
import com.robloxify.avatar.CosmeticCatalog;
import com.robloxify.avatar.CosmeticCategory;
import com.robloxify.client.ClientRobloxState;
import com.robloxify.client.RobloxifyClientNetworking;
import com.robloxify.util.Robux;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Shared layout for the Avatar editor, the Shop and the Inventory. */
public abstract class RobloxCatalogScreen extends RobloxScreen {
	public enum Mode {
		EQUIP,
		BUY,
		OWNED
	}

	protected static final int ROW_HEIGHT = 26;
	protected static final int ROW_GAP = 4;

	protected CosmeticCategory selected = CosmeticCategory.FACE;

	protected RobloxCatalogScreen(Component title) {
		super(title);
	}

	protected abstract Mode mode();

	protected List<CosmeticCategory> categories() {
		return List.of(CosmeticCategory.values());
	}

	protected boolean matches(Cosmetic cosmetic) {
		boolean owned = ClientRobloxState.owns(cosmetic.id());
		return switch (mode()) {
			case EQUIP -> true;
			case BUY -> !owned;
			case OWNED -> owned;
		};
	}

	@Override
	protected void init() {
		super.init();
		int x = CONTENT_X;
		int width = contentWidth();
		List<CosmeticCategory> categories = categories();
		int tabWidth = (width - (categories.size() - 1) * 4) / categories.size();

		for (int i = 0; i < categories.size(); i++) {
			CosmeticCategory category = categories.get(i);
			boolean active = category == selected;
			RobloxButton button = new RobloxButton(x + i * (tabWidth + 4), CONTENT_Y, tabWidth, 18,
					Component.literal(category.label()), () -> {
					selected = category;
					this.rebuildWidgets();
				}).accent(active ? RobloxTheme.ACCENT : RobloxTheme.BORDER);
			if (active) {
				button.primary();
			}
			addRenderableWidget(button);
		}

		List<Cosmetic> items = CosmeticCatalog.of(selected);
		int rowY = CONTENT_Y + 26;
		for (Cosmetic cosmetic : items) {
			if (!matches(cosmetic)) {
				continue;
			}
			boolean owned = ClientRobloxState.owns(cosmetic.id());
			boolean equipped = cosmetic.id().equals(ClientRobloxState.selfAppearance().slot(selected));
			String label = actionLabel(cosmetic, owned, equipped);
			if (label.isEmpty()) {
				rowY += ROW_HEIGHT + ROW_GAP;
				continue;
			}
			RobloxButton button = new RobloxButton(x + width - 92, rowY + 4, 88, 18,
					Component.literal(label), () -> onAction(cosmetic));
			button.accent(equipped ? RobloxTheme.ACCENT : RobloxTheme.BLUE);
			button.active = owned || mode() == Mode.BUY;
			addRenderableWidget(button);
			rowY += ROW_HEIGHT + ROW_GAP;
		}
	}

	protected String actionLabel(Cosmetic cosmetic, boolean owned, boolean equipped) {
		return switch (mode()) {
			case EQUIP -> equipped ? "UNEQUIP" : owned ? "EQUIP" : "LOCKED";
			case BUY -> owned ? "OWNED" : "BUY";
			case OWNED -> equipped ? "UNEQUIP" : "EQUIP";
		};
	}

	protected void onAction(Cosmetic cosmetic) {
		switch (mode()) {
			case BUY -> RobloxifyClientNetworking.send("buy", cosmetic.id());
			default -> RobloxifyClientNetworking.send("equip", cosmetic.id());
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int x = CONTENT_X;
		int width = contentWidth();
		List<Cosmetic> items = CosmeticCatalog.of(selected);
		int rowY = CONTENT_Y + 26;

		for (Cosmetic cosmetic : items) {
			if (!matches(cosmetic)) {
				continue;
			}
			boolean owned = ClientRobloxState.owns(cosmetic.id());
			boolean equipped = cosmetic.id().equals(ClientRobloxState.selfAppearance().slot(selected));
			RobloxUi.card(graphics, x, rowY, width, ROW_HEIGHT, equipped ? RobloxTheme.ACCENT : RobloxTheme.BORDER, equipped);
			graphics.text(this.font, cosmetic.name(), x + 12, rowY + 5, owned ? RobloxTheme.TEXT : RobloxTheme.TEXT_DIM);
			graphics.text(this.font, cosmetic.id(), x + 12, rowY + 15, RobloxTheme.TEXT_MUTED);

			if (cosmetic.primary() != 0) {
				graphics.fill(x + width - 116, rowY + 6, x + width - 100, rowY + 20, cosmetic.primary());
			}
			if (!owned) {
				String price = Robux.format(cosmetic.price());
				graphics.text(this.font, price, x + width - 130 - this.font.width(price), rowY + 9, RobloxTheme.GOLD);
			} else if (equipped) {
				graphics.text(this.font, "EQUIPPED", x + width - 130 - this.font.width("EQUIPPED"), rowY + 9,
						RobloxTheme.ACCENT_LIGHT);
			}
			rowY += ROW_HEIGHT + ROW_GAP;
		}
	}
}
