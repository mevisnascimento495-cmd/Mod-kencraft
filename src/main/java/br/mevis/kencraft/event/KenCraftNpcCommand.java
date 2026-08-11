package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcCommand {
    private KenCraftNpcCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("kencraft")
                .then(Commands.literal("npcs")
                        .executes(context -> spawnNpcs(context.getSource()))));
    }

    private static int spawnNpcs(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Esse comando precisa ser usado por um jogador."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        BlockPos base = player.blockPosition();

        spawnNpc(level, base.offset(2, 0, 0), "NPC • Onoki");
        spawnNpc(level, base.offset(-2, 0, 0), "NPC • Mestre Jio");

        source.sendSuccess(() -> Component.literal("KenCraft: NPCs criados perto de você."), true);
        return 1;
    }

    private static void spawnNpc(ServerLevel level, BlockPos pos, String displayName) {
        Villager npc = EntityType.VILLAGER.spawn(level, pos, MobSpawnType.COMMAND);
        if (npc == null) {
            return;
        }

        npc.setCustomName(Component.literal(displayName));
        npc.setCustomNameVisible(true);
        npc.setNoAi(true);
        npc.setInvulnerable(true);
        npc.setSilent(true);
        npc.setPersistenceRequired();
    }
}
