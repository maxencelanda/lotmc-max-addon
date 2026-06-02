package net.swimmingtuna.lotmc.marauder.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotmc.marauder.networking.ModNetworking;
import net.swimmingtuna.lotmc.marauder.networking.packet.PrometheusTheftChoiceC2S;

import java.util.List;

public class PrometheusTheftScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 22;

    private final List<String> abilityRegistryNames;
    private final int victimEntityId;
    private AbilityListWidget abilityList;

    public PrometheusTheftScreen(List<String> abilityRegistryNames, int victimEntityId) {
        super(Component.literal("Select an ability to steal"));
        this.abilityRegistryNames = abilityRegistryNames;
        this.victimEntityId = victimEntityId;
    }

    @Override
    protected void init() {
        abilityList = new AbilityListWidget(this, minecraft, this.width, 25, this.height - 40, BUTTON_HEIGHT + 2);
        for (String registryName : abilityRegistryNames) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
            if (item == null) continue;
            abilityList.addEntry(registryName, item.getName(new ItemStack(item)));
        }
        addRenderableWidget(abilityList);

        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.onClose())
                .bounds(this.width / 2 - BUTTON_WIDTH / 2, this.height - 30, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class AbilityListWidget extends AbstractSelectionList<AbilityListWidget.AbilityEntry> {
        private static final int ITEM_WIDTH = 220;

        private final PrometheusTheftScreen screen;

        public AbilityListWidget(PrometheusTheftScreen screen, Minecraft minecraft, int width, int y0, int y1, int itemHeight) {
            super(minecraft, width, y1 - y0, y0, y1, itemHeight);
            this.screen = screen;
            setRenderSelection(false);
        }

        @Override
        public void updateNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
        }

        public void addEntry(String registryName, Component displayName) {
            addEntry(new AbilityEntry(registryName, displayName));
        }

        @Override
        public int getRowWidth() {
            return ITEM_WIDTH;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.x1 - 6;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.renderList(guiGraphics, mouseX, mouseY, partialTick);
            this.renderDecorations(guiGraphics, mouseX, mouseY);
        }

        public class AbilityEntry extends AbstractSelectionList.Entry<AbilityEntry> {
            private final String registryName;
            private final Component displayName;

            public AbilityEntry(String registryName, Component displayName) {
                this.registryName = registryName;
                this.displayName = displayName;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
                int color = hovering ? 0xFFFFAA : 0xFFFFFF;
                guiGraphics.drawString(screen.font, displayName, left + width / 2 - screen.font.width(displayName) / 2, top + height / 2 - screen.font.lineHeight / 2, color);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    ModNetworking.INSTANCE.sendToServer(new PrometheusTheftChoiceC2S(registryName, screen.victimEntityId));
                    screen.onClose();
                    return true;
                }
                return false;
            }
        }
    }
}
