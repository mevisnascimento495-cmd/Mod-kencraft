package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.JioAnimationData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class JioSystem {
    public static final String NONE = "NONE";
    public static final String SEISHIN = "Seishin dan";
    public static final String HAKAI = "Hakai satsu Totetsu: Seimei kui";
    public static final String KATA = "Kata kyoka";
    private static final String[] TECHNIQUES = {SEISHIN, HAKAI, KATA};

    private JioSystem() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("kencraftjio")
                .then(Commands.literal("roll").executes(c -> roll(c.getSource())))
                .then(Commands.literal("next").executes(c -> nextAbility(c.getSource())))
                .then(Commands.literal("use").executes(c -> use(c.getSource())))
                .then(Commands.literal("charge").executes(c -> charge(c.getSource()))));
    }

    private static boolean eligible(PlayerData data) { return data.race() == Race.HUMAN && data.arfClass() >= 4; }

    private static int roll(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) { player.sendSystemMessage(Component.literal("Você precisa ser Investigador de Quarta Classe ou superior para usar Jio.")); return 0; }
        String current = PlayerData.normalizeTechnique(data.jioTechnique());
        if (!NONE.equals(current)) { player.sendSystemMessage(Component.literal("Você já girou sua técnica Jio e ganhou: " + current)); return 0; }
        String chosen = TECHNIQUES[player.getRandom().nextInt(TECHNIQUES.length)];
        player.setData(ModAttachments.PLAYER_DATA, data.withJioTechnique(chosen));
        player.sendSystemMessage(Component.literal("Você girou sua técnica Jio e ganhou: " + chosen));
        player.sendSystemMessage(Component.literal("Use G para trocar de habilidade e F para usar a habilidade selecionada."));
        return 1;
    }

    private static int nextAbility(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) return 0;
        String technique = PlayerData.normalizeTechnique(data.jioTechnique());
        if (NONE.equals(technique)) { player.sendSystemMessage(Component.literal("Primeiro gire sua técnica Jio no menu R.")); return 0; }
        int next = (data.jioAbilitySlot() + 1) % 3;
        player.setData(ModAttachments.PLAYER_DATA, data.withJioAbilitySlot(next));
        player.sendSystemMessage(Component.literal(technique + " — Habilidade " + (next + 1) + "/3 selecionada."));
        return 1;
    }

    private static int use(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) return 0;
        String technique = PlayerData.normalizeTechnique(data.jioTechnique());
        if (NONE.equals(technique)) { player.sendSystemMessage(Component.literal("Você ainda não possui uma técnica Jio. Gire uma no menu R.")); return 0; }
        int techniqueIndex = indexOf(technique), slot = data.jioAbilitySlot(), cost = cost(techniqueIndex, slot);
        if (data.jio() < cost) { player.sendSystemMessage(Component.literal("Jio insuficiente. Custo desta habilidade: " + cost + " Jio.")); return 0; }
        player.setData(ModAttachments.PLAYER_DATA, data.withJio(data.jio() - cost, data.calculatedHumanMaxJio()));
        int duration = animationDuration(techniqueIndex, slot);
        if (duration > 0) player.setData(ModAttachments.JIO_ANIMATION, new JioAnimationData(technique, slot, player.level().getGameTime(), duration));
        if (techniqueIndex == 0 && slot == 2) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 100, 4, false, true));
        } else if (techniqueIndex == 2 && slot == 0) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 240, 2, false, true));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 240, 1, false, true));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 240, 1, false, true));
        }
        return 1;
    }

    private static int charge(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) return 0;
        int max = data.calculatedHumanMaxJio();
        if (data.jio() < max) player.setData(ModAttachments.PLAYER_DATA, data.withJio(Math.min(max, data.jio() + 2), max));
        return 1;
    }

    static int indexOf(String technique) {
        for (int i = 0; i < TECHNIQUES.length; i++) if (TECHNIQUES[i].equals(technique)) return i;
        return -1;
    }

    static int cost(int technique, int slot) {
        if (technique == 0 && slot == 2) return 50;
        if (technique == 1 && slot == 2) return 100;
        return 30;
    }

    static int animationDuration(int technique, int slot) {
        if (technique == 0) return switch (slot) { case 0 -> 14; case 1 -> 80; case 2 -> 100; default -> 0; };
        if (technique == 1) return switch (slot) { case 0 -> 16; case 1 -> 140; case 2 -> 14; default -> 0; };
        if (technique == 2) return switch (slot) { case 1 -> 28; case 2 -> 42; default -> 0; };
        return 0;
    }
}
