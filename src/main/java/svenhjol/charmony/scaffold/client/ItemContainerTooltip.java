package svenhjol.charmony.scaffold.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import svenhjol.charmony.scaffold.Charmony;

import java.util.List;

@SuppressWarnings("unused")
public interface ItemContainerTooltip extends TooltipComponent {
    ResourceLocation BACKGROUND_SPRITE = ResourceLocation.fromNamespaceAndPath(Charmony.ID, "item_container/background");
    int MARGIN_Y = 6;
    int SLOT_SIZE_X = 18;
    int SLOT_SIZE_Y = 20;

    int gridSizeX();

    int gridSizeY();

    List<ItemStack> getItems();

    default int backgroundWidth() {
        return this.gridSizeX() * SLOT_SIZE_X + 2;
    }

    default int backgroundHeight() {
        return this.gridSizeY() * SLOT_SIZE_Y;
    }

    default int marginY() {
        return MARGIN_Y;
    }

    default void defaultRenderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        var gx = this.gridSizeX();
        var gy = this.gridSizeY();
        guiGraphics.blitSprite(RenderType::guiTextured, BACKGROUND_SPRITE, x, y, backgroundWidth(), backgroundHeight());
        int index = 0;

        for (int yy = 0; yy < gy; ++yy) {
            for (int xx = 0; xx < gx; ++xx) {
                int slotX = x + xx * 18 + 1;
                int slotY = y + yy * 20 + 1;
                renderSlotWithIndex(guiGraphics, font, index++, slotX, slotY);
            }
        }
    }

    default void renderSlotWithIndex(GuiGraphics guiGraphics, Font font, int index, int x, int y) {
        if (index >= getItems().size()) {
            renderSlot(guiGraphics, x, y);
        } else {
            var itemStack = getItems().get(index);
            renderSlot(guiGraphics, x, y);
            guiGraphics.renderItem(itemStack, x + 1, y + 1, index);
            guiGraphics.renderItemDecorations(font, itemStack, x + 1, y + 1);
        }
    }

    default void renderSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blitSprite(RenderType::guiTextured, Texture.SLOT.sprite, x, y, Texture.SLOT.width, Texture.SLOT.height);
    }

    enum Texture {
        SLOT(ResourceLocation.fromNamespaceAndPath(Charmony.ID, "item_container/slot"), SLOT_SIZE_X, SLOT_SIZE_Y);

        public final ResourceLocation sprite;
        public final int width;
        public final int height;

        Texture(final ResourceLocation resourceLocation, final int width, final int height) {
            this.sprite = resourceLocation;
            this.width = width;
            this.height = height;
        }
    }
}