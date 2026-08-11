package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcInteraction {
    private KenCraftNpcInteraction() {}

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (!villager.hasCustomName()) return;

        String name = villager.getCustomName().getString();
        if (!KenCraftNpcCommand.ARF_NAME.equals(name) && !KenCraftNpcCommand.RINKA_NAME.equals(name)) return;

        event.setCanceled(true);

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        int mental = data.mentalXp();
        int physical = data.physicalXp();

        if (KenCraftNpcCommand.ARF_NAME.equals(name)) {
            mental += 5;
            player.sendSystemMessage(Component.literal("ARF: +5 XP Mental. Use o menu R para distribuir em Inteligência/Percepção/Desenvolvimento espiritual."));
        } else {
            physical += 5;
            player.sendSystemMessage(Component.literal("Rinka: +5 XP Física. Use o menu R para distribuir em Força/Defesa/Velocidade/Genética."));
        }

        player.setData(ModAttachments.PLAYER_DATA, data.withXp(mental, physical));
    }
}
