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
        int width = 188;
        int lineHeight = 12;
        int statusCount = data.race() == br.mevis.kencraft.data.Race.RINKA ? 5 : 5;
        boolean noRace = data.race() == br.mevis.kencraft.data.Race.NONE;
        int height = noRace ? 58 : 32 + (statusCount * lineHeight) + (data.race() == br.mevis.kencraft.data.Race.HUMAN ? lineHeight : 0);

        graphics.fill(x, y, x + width, y + height, 0xB0101018);
        graphics.fill(x, y, x + width, y + 2, 0xFF7AD7FF);

        String race = switch (data.race()) {
            case RINKA -> "Rinka";
            case HUMAN -> "Humano";
            case NONE -> "Sem raça";
        };

        int health = (int) Math.ceil(minecraft.player.getHealth());
        int maxHealth = (int) Math.ceil(minecraft.player.getMaxHealth());

        graphics.drawString(minecraft.font, Component.literal("KenCraft"), x + 7, y + 6, 0xFF7AD7FF);
        graphics.drawString(minecraft.font, Component.literal("Raça: " + race), x + 7, y + 19, 0xFFFFFFFF);

        if (noRace) {
            graphics.drawString(minecraft.font, Component.literal("Digite Rinka ou Humano"), x + 7, y + 34, 0xFFFFD166);
            graphics.drawString(minecraft.font, Component.literal("Vida: " + health + "/" + maxHealth), x + 7, y + 47, 0xFFFFFFFF);
            return;
        }

        int currentY = y + 32;

        if (data.race() == br.mevis.kencraft.data.Race.RINKA) {
            currentY = drawStatus(graphics, minecraft, x, currentY, "Força", data.strength());
            currentY = drawStatus(graphics, minecraft, x, currentY, "Defesa/Resistência", data.defense());
            currentY = drawStatus(graphics, minecraft, x, currentY, "Inteligência", data.intelligence());
            currentY = drawStatus(graphics, minecraft, x, currentY, "Velocidade", data.speed());
            currentY = drawStatus(graphics, minecraft, x, currentY, "Genética", data.genetics());
            graphics.drawString(minecraft.font, Component.literal("Vida: " + health + "/" + maxHealth), x + 7, currentY + 1, 0xFFFFFFFF);
        } else {
            currentY = drawStatus(graphics, minecraft, x, currentY, "Força", data.strength());
            currentY = drawStatus(graphics, minecraft, x, currentY, "Percepção", data.perception());
            currentY = drawStatus(graphics, minecraft, x, currentY, "Desenvolvimento espiritual", data.spiritualDevelopment());
            currentY = drawStatus(graphics, minecraft, x, currentY, "Velocidade", data.speed());
            graphics.drawString(minecraft.font, Component.literal("Vida: " + health + "/" + maxHealth), x + 7, currentY + 1, 0xFFFFFFFF);
            graphics.drawString(minecraft.font, Component.literal("Jio: " + data.jio() + "/" + data.calculatedHumanMaxJio()), x + 7, currentY + 14, 0xFF7AD7FF);
            graphics.drawString(minecraft.font, Component.literal("XP Mental: " + data.mentalXp() + "  XP Física: " + data.physicalXp()), x + 7, currentY + 27, 0xFFB8C5D1);
        }
    }

    private static int drawStatus(GuiGraphics graphics, Minecraft minecraft, int x, int y, String name, int value) {
        graphics.drawString(minecraft.font,
                Component.literal(name + ": " + value + "/" + PlayerData.MAX_STATUS + " (" + PlayerData.spentPoints(value) + " points)"),
                x + 7,
                y,
                0xFFFFFFFF);
        return y + 12;
    }
}
