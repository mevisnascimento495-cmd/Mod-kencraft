package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.event.SpiritualTrainingSystem;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpiritualTrainingCommand {
    private SpiritualTrainingCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("kencraftjio")
                .then(Commands.literal("train").executes(context -> {
                    if (!(context.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return 0;
                    return SpiritualTrainingSystem.startTraining(player);
                })));
    }
}
