package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftStatusCommand {
    private KenCraftStatusCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("kencraft")
                .then(Commands.literal("status")
                        .then(Commands.literal("add")
                                .then(Commands.argument("attribute", StringArgumentType.word())
                                        .executes(context -> addPoint(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "attribute")))))));
    }

    private static int addPoint(CommandSourceStack source, String attribute) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Esse comando precisa ser usado por um jogador."));
            return 0;
        }

        String key = normalize(attribute);
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);

        if (!data.hasRace()) {
            source.sendFailure(Component.literal("Escolha uma raça antes de distribuir status."));
            return 0;
        }

        boolean mental = isMental(data.race(), key);
        boolean physical = isPhysical(data.race(), key);
        if (!mental && !physical) {
            source.sendFailure(Component.literal("Esse status não pertence ao sistema da sua raça."));
            return 0;
        }

        int xp = mental ? data.mentalXp() : data.physicalXp();
        int current = getStatus(data, key);
        if (xp <= 0) {
            source.sendFailure(Component.literal("Você não possui XP suficiente para esse status."));
            return 0;
        }
        if (current >= PlayerData.MAX_STATUS) {
            source.sendFailure(Component.literal("Esse status já está no máximo (20)."));
            return 0;
        }

        PlayerData updated = data.withStatus(key, current + 1);
        updated = updated.withXp(mental ? data.mentalXp() - 1 : data.mentalXp(),
                physical ? data.physicalXp() - 1 : data.physicalXp());
        player.setData(ModAttachments.PLAYER_DATA, updated);

        source.sendSuccess(() -> Component.literal("KenCraft: " + displayName(key) + " agora está em " + (current + 1) + "/20."), true);
        return 1;
    }

    private static String normalize(String value) {
        return switch (value.toLowerCase()) {
            case "forca", "força" -> "strength";
            case "defesa", "resistencia", "resistência" -> "defense";
            case "inteligencia", "inteligência" -> "intelligence";
            case "velocidade" -> "speed";
            case "genetica", "genética" -> "genetics";
            case "percepcao", "percepção" -> "perception";
            case "espiritual", "desenvolvimentoespiritual" -> "spiritual";
            case "vida" -> "life";
            default -> value.toLowerCase();
        };
    }

    private static boolean isMental(Race race, String key) {
        return race == Race.RINKA ? key.equals("intelligence") : key.equals("perception") || key.equals("spiritual");
    }

    private static boolean isPhysical(Race race, String key) {
        if (race == Race.RINKA) {
            return key.equals("strength") || key.equals("defense") || key.equals("speed") || key.equals("genetics");
        }
        return key.equals("strength") || key.equals("speed") || key.equals("life");
    }

    private static int getStatus(PlayerData data, String key) {
        return switch (key) {
            case "strength" -> data.strength();
            case "defense" -> data.defense();
            case "intelligence" -> data.intelligence();
            case "speed" -> data.speed();
            case "genetics" -> data.genetics();
            case "perception" -> data.perception();
            case "spiritual" -> data.spiritualDevelopment();
            case "life" -> data.life();
            default -> 0;
        };
    }

    private static String displayName(String key) {
        return switch (key) {
            case "strength" -> "Força";
            case "defense" -> "Defesa/Resistência";
            case "intelligence" -> "Inteligência";
            case "speed" -> "Velocidade";
            case "genetics" -> "Genética";
            case "perception" -> "Percepção";
            case "spiritual" -> "Desenvolvimento espiritual";
            case "life" -> "Vida";
            default -> key;
        };
    }
}
