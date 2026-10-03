package br.mevis.kencraft.client;

import br.mevis.kencraft.item.KenCraftItems;
import br.mevis.kencraft.menu.ArtifactShopMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class ArtifactShopScreen extends AbstractContainerScreen<ArtifactShopMenu> {
    private static final int WIDTH = 430, HEIGHT = 280;
    public ArtifactShopScreen(ArtifactShopMenu menu, Inventory inv, Component title) { super(menu, inv, title); imageWidth = WIDTH; imageHeight = HEIGHT; }
    @Override protected void init() {
        super.init();
        int left = (width - WIDTH) / 2, top = (height - HEIGHT) / 2;
        addRenderableWidget(Button.builder(Component.literal("Comprar — 8 esmeraldas"), b -> buy(0)).bounds(left + 115, top + 175, 200, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Comprar — 8 esmeraldas"), b -> buy(1)).bounds(left + 25, top + 205, 185, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Comprar — 12 esmeraldas"), b -> buy(2)).bounds(left + 220, top + 205, 185, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Comprar — 16 esmeraldas"), b -> buy(3)).bounds(left + 25, top + 235, 185, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Comprar — 24 esmeraldas"), b -> buy(4)).bounds(left + 220, top + 235, 185, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> Minecraft.getInstance().player.closeContainer()).bounds(left + 165, top + 260, 100, 20).build());
    }
    private void buy(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) mc.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }
    @Override protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        int left = (width - WIDTH) / 2, top = (height - HEIGHT) / 2;
        g.fill(0, 0, width, height, 0x99000000);
        g.fill(left, top, left + WIDTH, top + HEIGHT, 0xF01B1B22);
        g.fill(left + 10, top + 10, left + WIDTH - 10, top + 42, 0xFF3B3023);
        g.fill(left + 10, top + 52, left + WIDTH - 10, top + 165, 0xCC11141A);
        g.drawCenteredString(font, Component.literal("LOJA DE ARTEFATOS"), width / 2, top + 18, 0xFFFFD66B);
        g.drawCenteredString(font, Component.literal("Mercador • armas, acessórios e artefatos"), width / 2, top + 31, 0xFFD0D5DD);
        drawItem(g, new ItemStack(KenCraftItems.KATANA_COMUM.get()), left + 207, top + 62, "Katana Comum");
        drawItem(g, new ItemStack(KenCraftItems.SPIRIT_NECKLACE.get()), left + 45, top + 115, "Colar do Espírito");
        drawItem(g, new ItemStack(KenCraftItems.VITALITY_RING.get()), left + 145, top + 115, "Anel da Vitalidade");
        drawItem(g, new ItemStack(KenCraftItems.PERCEPTION_AMULET.get()), left + 265, top + 115, "Amuleto da Percepção");
        drawItem(g, new ItemStack(KenCraftItems.PARADISE_FRAGMENT.get()), left + 355, top + 115, "Fragmento do Paraíso");
    }
    private void drawItem(GuiGraphics g, ItemStack stack, int x, int y, String name) {
        g.renderItem(stack, x, y);
        g.drawString(font, Component.literal(name), x + 22, y + 5, 0xFFFFFFFF);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
    @Override public void render(GuiGraphics g, int mx, int my, float partial) { renderBackground(g, mx, my, partial); super.render(g, mx, my, partial); }
}