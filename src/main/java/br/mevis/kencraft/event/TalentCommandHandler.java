package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class TalentCommandHandler {
    private TalentCommandHandler() {}

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("kencraft")
                        .then(Commands.literal("talent")
                                .then(Commands.literal("use")
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            TalentSystem.use(player);
                                            return Command.SINGLE_SUCCESS;
                                        })))
        );
    }
}
