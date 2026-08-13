package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
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
public final class KenCraftJioCommand {
    private static final String TECH_SEISHIN = "Seishin Dan";
    private static final String TECH_HAKAI = "Hakai Satsu Totetsu: Seimei kui";
    private static final String TECH_KATA = "Kata kyoka";
    private static final String[] TECHNIQUES = {TECH_SEISHIN, TECH_HAKAI, TECH_KATA};

    private KenCraftJioCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("kencraft").then(Commands.literal("jio")
                .then(Commands.literal("random").executes(c -> random(c.getSource())))
                .then(Commands.literal("cycle").executes(c -> cycleAbility(c.getSource())))
                .then(Commands.literal("attack").executes(c -> attack(c.getSource())))
                .then(Commands.literal("charge").executes(c -> charge(c.getSource())))));
    }

    private static boolean eligible(PlayerData data) {
        return data.race() == Race.HUMAN && data.arfClass() >= 4;
    }

    private static boolean isLegacyTechnique(String technique) {
        return "Barreira".equals(technique) || "Rajada".equals(technique) || "Reforço".equals(technique)
                || "Seishin Dan".equals(technique) == false && "Hakai Satsu Totetsu: Seimei kui".equals(technique) == false
                && "Kata kyoka".equals(technique) == false && !"NONE".equals(technique);
    }

    private static int random(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) {
            player.sendSystemMessage(Component.literal("Você precisa ser Investigador de Quarta Classe ou superior para usar Jio."));
            return 0;
        }

        // Migra saves das versões antigas que tinham Barreira/Rajada/Reforço.
        if (isLegacyTechnique(data.jioTechnique())) {
            data = data.withJioTechnique("NONE");
            player.setData(ModAttachments.PLAYER_DATA, data);
        }

        if (!"NONE".equals(data.jioTechnique())) {
            player.sendSystemMessage(Component.literal("Você já girou sua técnica Jio e ganhou: " + data.jioTechnique()));
            return 0;
        }

        String chosen = TECHNIQUES[player.getRandom().nextInt(TECHNIQUES.length)];
        player.setData(ModAttachments.PLAYER_DATA, data.withJioTechnique(chosen));
        player.sendSystemMessage(Component.literal("Você girou sua técnica Jio e ganhou: " + chosen));
        player.sendSystemMessage(Component.literal("Use G para trocar de habilidade e F para usar a habilidade selecionada."));
        return 1;
    }

    private static int cycleAbility(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) {
            player.sendSystemMessage(Component.literal("Você precisa ser Investigador de Quarta Classe ou superior para usar Jio."));
            return 0;
        }
        if (isLegacyTechnique(data.jioTechnique())) {
            player.setData(ModAttachments.PLAYER_DATA, data.withJioTechnique("NONE"));
            player.sendSystemMessage(Component.literal("Sua técnica antiga foi atualizada. Abra o menu R e gire novamente."));
            return 0;
        }
        if ("NONE".equals(data.jioTechnique())) {
            player.sendSystemMessage(Component.literal("Primeiro gire sua técnica Jio no menu R."));
            return 0;
        }
        int next = (data.jioAbilitySlot() + 1) % 3;
        player.setData(ModAttachments.PLAYER_DATA, data.withJioAbilitySlot(next));
        player.sendSystemMessage(Component.literal(data.jioTechnique() + " — Habilidade " + (next + 1) + "/3 selecionada."));
        return 1;
    }

    private static int attack(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) return 0;
        if (isLegacyTechnique(data.jioTechnique())) {
            player.setData(ModAttachments.PLAYER_DATA, data.withJioTechnique("NONE"));
            player.sendSystemMessage(Component.literal("Sua técnica antiga foi atualizada. Abra o menu R e gire novamente."));
            return 0;
        }
        if ("NONE".equals(data.jioTechnique())) {
            player.sendSystemMessage(Component.literal("Você ainda não possui uma técnica Jio. Gire uma no menu R."));
            return 0;
        }
        int technique = techniqueIndex(data.jioTechnique());
        if (technique < 0) {
            player.setData(ModAttachments.PLAYER_DATA, data.withJioTechnique("NONE"));
            player.sendSystemMessage(Component.literal("Sua técnica antiga foi atualizada. Abra o menu R e gire novamente."));
            return 0;
        }
        int slot = data.jioAbilitySlot();
        int cost = abilityCost(technique, slot);
        if (data.jio() < cost) {
            player.sendSystemMessage(Component.literal("Jio insuficiente. Custo desta habilidade: " + cost + " Jio."));
            return 0;
        }
        player.setData(ModAttachments.PLAYER_DATA, data.withJio(data.jio() - cost, data.calculatedHumanMaxJio()));
        executeAbility(player, technique, slot);
        return 1;
    }

    private static int abilityCost(int technique, int slot) {
        if (technique == 1 && slot == 2) return 100;
        if (technique == 0 && slot == 2) return 50;
        return 30;
    }

    private static void executeAbility(ServerPlayer player, int technique, int slot) {
        switch (technique) {
            case 0 -> executeSeishinDan(player, slot);
            case 1 -> executeSeimeiKui(player, slot);
            case 2 -> executeKataKyoka(player, slot);
            default -> player.sendSystemMessage(Component.literal("Técnica Jio inválida."));
        }
    }

    private static void executeSeishinDan(ServerPlayer player, int slot) {
        int spiritual = player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment();
        if (slot == 0) {
            LivingEntity target = findTarget(player, 24.0D);
            if (target != null) target.hurt(player.damageSources().magic(), 5.0F + spiritual * 1.5F);
            player.sendSystemMessage(Component.literal("Seishin Dan — Tiro de espiritual!"));
        } else if (slot == 1) {
            for (int i = 0; i < 6; i++) {
                LivingEntity target = findTarget(player, 24.0D);
                if (target == null) break;
                target.hurt(player.damageSources().magic(), 4.0F + spiritual * 1.2F);
            }
            player.sendSystemMessage(Component.literal("Seishin Dan — Metralhadora espiritual!"));
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 5, 4, false, true));
            player.sendSystemMessage(Component.literal("Seishin Dan — Intangibilidade espiritual!"));
        }
    }

    private static void executeSeimeiKui(ServerPlayer player, int slot) {
        int spiritual = player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment();
        LivingEntity target = findTarget(player, 5.0D);
        if (slot == 0) {
            if (target != null) {
                target.hurt(player.damageSources().playerAttack(player), 8.0F + spiritual);
                target.setDeltaMovement(target.getDeltaMovement().add(player.getLookAngle().scale(1.4D)));
            }
            player.sendSystemMessage(Component.literal("Hakai Satsu Totetsu: Seimei kui — Soco explosivo!"));
        } else if (slot == 1) {
            if (target != null) {
                for (int i = 0; i < 7; i++) target.hurt(player.damageSources().playerAttack(player), 3.5F + spiritual * 0.5F);
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 100));
            }
            player.sendSystemMessage(Component.literal("Hakai Satsu Totetsu: Seimei kui — Barragem de golpes da chama da luta!"));
        } else {
            if (target != null) target.hurt(player.damageSources().playerAttack(player), 70.0F);
            player.sendSystemMessage(Component.literal("Hakai Satsu Totetsu: Seimei kui — Destruição total!"));
        }
    }

    private static void executeKataKyoka(ServerPlayer player, int slot) {
        if (slot == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 12, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 12, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 12, 1, false, true));
            player.sendSystemMessage(Component.literal("Kata kyoka — Reforço ativado!"));
        } else {
            LivingEntity target = findTarget(player, 4.0D);
            if (target != null) {
                if (slot == 1) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 10));
                    target.hurt(player.damageSources().playerAttack(player), 6.0F);
                    player.sendSystemMessage(Component.literal("Kata kyoka — Mão esmagadora!"));
                } else {
                    for (int i = 0; i < 8; i++) target.hurt(player.damageSources().playerAttack(player), 3.0F);
                    player.sendSystemMessage(Component.literal("Kata kyoka — Combo de reforço!"));
                }
            }
        }
    }

    private static int charge(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) return 0;
        int max = data.calculatedHumanMaxJio();
        if (data.jio() < max) player.setData(ModAttachments.PLAYER_DATA, data.withJio(Math.min(max, data.jio() + 2), max));
        return 1;
    }

    private static LivingEntity findTarget(ServerPlayer player, double range) {
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

    private static int techniqueIndex(String technique) {
        for (int i = 0; i < TECHNIQUES.length; i++) if (TECHNIQUES[i].equals(technique)) return i;
        return -1;
    }
}
