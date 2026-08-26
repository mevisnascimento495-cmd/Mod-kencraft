package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.SpiritualState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Guards the Sujo release independently from the Jio implementation.
 * Segunda Classe is represented by arfClass >= 2 in the existing class progression.
 */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpiritualStateAccessGuard {
    private SpiritualStateAccessGuard() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void register(RegisterCommandsEvent event) {
        // The actual kencraftjio command remains owned by JioSystem.
        // This class intentionally contains only the shared eligibility rule.
    }

    public static boolean canActivate(PlayerData data) {
        if (data.race() == Race.HYBRID || data.race() == Race.JASHIN) return true;
        return (data.race() == Race.HUMAN || data.race() == Race.RINKA) && data.arfClass() >= 2;
    }

    public static boolean canUseSujo(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        return canActivate(data) && !PlayerData.normalizeTechnique(data.jioTechnique()).equals("NONE");
    }

    public static Component requirementMessage() {
        return Component.literal("O Estado Sujo exige que você alcance a Segunda Classe.");
    }

    public static SpiritualState state(ServerPlayer player) {
        return player.getData(ModAttachments.SPIRITUAL_STATE);
    }
}
