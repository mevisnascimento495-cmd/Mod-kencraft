package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class KenCraftStatusHud {
    private KenCraftStatusHud() {}

    @SubscribeEvent
    public static void onHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        PlayerData data = minecraft.player.getData(ModAttachments.PLAYER_DATA);
        GuiGraphics graphics = event.getGuiGraphics();

        int x = 8;
        int y = 8;
        int width = 150;
        int height = 58;

        graphics.fill(x, y, x + width, y + height, 0xB0101018);
        graphics.fill(x, y, x + width, y + 2, 0xFF7AD7FF);

        String race = switch (data.race()) {
            case RINKA -> "Rinka";
            case HUMAN -> "Humano";
            case NONE -> "Sem raça";
        };

        int health = (int) Math.ceil(minecraft.player.getHealth());
        int maxHealth = (int) Math.ceil(minecraft.player.getMaxHealth());

        graphics.drawString(minecraft.font, Component.literal("KenCraft"), x + 7, y + 7, 0xFF7AD7FF);
        graphics.drawString(minecraft.font, Component.literal("Raça: " + race), x + 7, y + 20, 0xFFFFFFFF);
        graphics.drawString(minecraft.font, Component.literal("Jio: " + data.jio() + "/" + data.maxJio()), x + 7, y + 33, 0xFFFFFFFF);
        graphics.drawString(minecraft.font, Component.literal("Vida: " + health + "/" + maxHealth), x + 7, y + 46, 0xFFFFFFFF);
    }
}
