package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Reserved training entry point. The training mechanics will be implemented later. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpiritualTrainingCommand {
    private SpiritualTrainingCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("kencraftjio")
                .then(Commands.literal("train").executes(context -> {
                    if (context.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                        player.sendSystemMessage(Component.literal("Treinamento do Estado Espiritual ainda não foi implementado."));
                    }
                    return 1;
                })));
    }
}
