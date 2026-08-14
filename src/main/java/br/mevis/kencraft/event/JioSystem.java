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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
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

    private static boolean eligible(PlayerData data) {
        return data.race() == Race.HUMAN && data.arfClass() >= 4;
    }

    private static int roll(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) {
            player.sendSystemMessage(Component.literal("Você precisa ser Investigador de Quarta Classe ou superior para usar Jio."));
            return 0;
        }

        String current = PlayerData.normalizeTechnique(data.jioTechnique());
        if (!NONE.equals(current)) {
            player.sendSystemMessage(Component.literal("Você já girou sua técnica Jio e ganhou: " + current));
            return 0;
        }

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
        if (NONE.equals(technique)) {
            player.sendSystemMessage(Component.literal("Primeiro gire sua técnica Jio no menu R."));
            return 0;
        }
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
        if (NONE.equals(technique)) {
            player.sendSystemMessage(Component.literal("Você ainda não possui uma técnica Jio. Gire uma no menu R."));
            return 0;
        }
        int techniqueIndex = indexOf(technique);
        int slot = data.jioAbilitySlot();
        int cost = cost(techniqueIndex, slot);
        if (data.jio() < cost) {
            player.sendSystemMessage(Component.literal("Jio insuficiente. Custo desta habilidade: " + cost + " Jio."));
            return 0;
        }

        player.setData(ModAttachments.PLAYER_DATA, data.withJio(data.jio() - cost, data.calculatedHumanMaxJio()));
        int duration = animationDuration(techniqueIndex, slot);
        if (duration > 0) {
            // Server-authoritative and synchronized through the attachment so every tracking client
            // renders the same attack on the same player.
            player.setData(ModAttachments.JIO_ANIMATION, new JioAnimationData(
                    technique, slot, player.level().getGameTime(), duration));
        }
        execute(player, techniqueIndex, slot);
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

    private static int indexOf(String technique) {
        for (int i = 0; i < TECHNIQUES.length; i++) if (TECHNIQUES[i].equals(technique)) return i;
        return -1;
    }

    private static int cost(int technique, int slot) {
        if (technique == 0 && slot == 2) return 50;
        if (technique == 1 && slot == 2) return 100;
        return 30;
    }

    private static int animationDuration(int technique, int slot) {
        if (technique == 0) return switch (slot) { case 0 -> 14; case 1 -> 80; case 2 -> 100; default -> 0; };
        if (technique == 1) return switch (slot) { case 0 -> 16; case 1 -> 140; case 2 -> 14; default -> 0; };
        if (technique == 2) return switch (slot) { case 1 -> 28; case 2 -> 42; default -> 0; };
        return 0;
    }

    private static void execute(ServerPlayer player, int technique, int slot) {
        if (technique == 0) seishin(player, slot);
        else if (technique == 1) hakai(player, slot);
        else if (technique == 2) kata(player, slot);
    }

    private static void seishin(ServerPlayer player, int slot) {
        int spiritual = player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment();
        LivingEntity target = target(player, 24.0D);
        if (slot == 0) {
            if (target != null) target.hurt(player.damageSources().magic(), 5.0F + spiritual * 1.5F);
            player.sendSystemMessage(Component.literal("Seishin dan — Tiro de espiritual!"));
        } else if (slot == 1) {
            for (int i = 0; i < 6 && target != null; i++) target.hurt(player.damageSources().magic(), 4.0F + spiritual * 1.2F);
            player.sendSystemMessage(Component.literal("Seishin dan — Metralhadora espiritual!"));
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, true));
            player.sendSystemMessage(Component.literal("Seishin dan — Intangibilidade espiritual!"));
        }
    }

    private static void hakai(ServerPlayer player, int slot) {
        int spiritual = player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment();
        LivingEntity target = target(player, 5.0D);
        if (slot == 0) {
            if (target != null) {
                target.hurt(player.damageSources().playerAttack(player), 8.0F + spiritual);
                target.setDeltaMovement(target.getDeltaMovement().add(player.getLookAngle().scale(1.4D)));
            }
            player.sendSystemMessage(Component.literal("Hakai satsu Totetsu: Seimei kui — Soco explosivo!"));
        } else if (slot == 1) {
            if (target != null) {
                for (int i = 0; i < 7; i++) target.hurt(player.damageSources().playerAttack(player), 3.5F + spiritual * 0.5F);
                target.setRemainingFireTicks(Math.max(100, target.getRemainingFireTicks()));
            }
            player.sendSystemMessage(Component.literal("Hakai satsu Totetsu: Seimei kui — Barragem de golpes da chama da luta!"));
        } else {
            if (target != null) target.hurt(player.damageSources().playerAttack(player), 70.0F);
            player.sendSystemMessage(Component.literal("Hakai satsu Totetsu: Seimei kui — Destruição total!"));
        }
    }

    private static void kata(ServerPlayer player, int slot) {
        LivingEntity target = target(player, 4.0D);
        if (slot == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1, false, true));
            player.sendSystemMessage(Component.literal("Kata kyoka — Reforço ativado!"));
        } else if (slot == 1) {
            if (target != null) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 10));
                target.hurt(player.damageSources().playerAttack(player), 6.0F);
            }
            player.sendSystemMessage(Component.literal("Kata kyoka — Mão esmagadora!"));
        } else {
            if (target != null) for (int i = 0; i < 8; i++) target.hurt(player.damageSources().playerAttack(player), 3.0F);
            player.sendSystemMessage(Component.literal("Kata kyoka — Combo de reforço!"));
        }
    }

    private static LivingEntity target(ServerPlayer player, double range) {
        var eye = player.getEyePosition();
        var end = eye.add(player.getLookAngle().scale(range));
        var box = player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1.0D);
        LivingEntity best = null;
        double bestDistance = range * range;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
            var hit = entity.getBoundingBox().inflate(0.3D).clip(eye, end);
            if (hit.isPresent()) {
                double distance = eye.distanceToSqr(hit.get());
                if (distance < bestDistance) { bestDistance = distance; best = entity; }
            }
        }
        return best;
    }
}
