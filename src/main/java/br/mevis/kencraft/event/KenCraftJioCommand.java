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
    private static final String[] TECHNIQUES = {"Kata Kyōka 体強化", "Seishin Dan 精神弾", "生命喰 Hakai Satsu Tōtetsu: Seimei Kui"};
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

    private static boolean eligible(PlayerData data) { return data.race() == Race.HUMAN && data.arfClass() >= 4; }

    private static int random(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) { player.sendSystemMessage(Component.literal("Você precisa ser Investigador de Quarta Classe ou superior para usar Jio.")); return 0; }
        if (!"NONE".equals(data.jioTechnique())) { player.sendSystemMessage(Component.literal("Você já possui uma técnica Jio: " + data.jioTechnique() + ".")); return 0; }
        String chosen = TECHNIQUES[player.getRandom().nextInt(TECHNIQUES.length)];
        player.setData(ModAttachments.PLAYER_DATA, data.withJioTechnique(chosen));
        player.sendSystemMessage(Component.literal("Técnica Jio obtida: " + chosen));
        player.sendSystemMessage(Component.literal("Use G para trocar a habilidade e F para usar a habilidade selecionada."));
        return 1;
    }

    private static int cycleAbility(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) return 0;
        if ("NONE".equals(data.jioTechnique())) { player.sendSystemMessage(Component.literal("Primeiro gire sua técnica Jio no menu R.")); return 0; }
        int next = (data.jioAbilitySlot() + 1) % 3;
        player.setData(ModAttachments.PLAYER_DATA, data.withJioAbilitySlot(next));
        player.sendSystemMessage(Component.literal("Técnica: " + data.jioTechnique() + " | Habilidade " + (next + 1) + "/3 selecionada."));
        return 1;
    }

    private static int attack(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data) || "NONE".equals(data.jioTechnique())) return 0;
        int technique = techniqueIndex(data.jioTechnique());
        int slot = data.jioAbilitySlot();
        int cost = abilityCost(technique, slot);
        if (data.jio() < cost) { player.sendSystemMessage(Component.literal("Jio insuficiente. Esta habilidade custa " + cost + " Jio.")); return 0; }
        player.setData(ModAttachments.PLAYER_DATA, data.withJio(data.jio() - cost, data.calculatedHumanMaxJio()));
        executeAbility(player, technique, slot);
        return 1;
    }

    private static int abilityCost(int technique, int slot) {
        // Hakai Satsu ability 3 costs 100; Seishin Dan ability 3 costs 50; all others cost 30.
        if (technique == 2 && slot == 2) return 100;
        if (technique == 1 && slot == 2) return 50;
        return 30;
    }

    private static void executeAbility(ServerPlayer player, int technique, int slot) {
        switch (technique) {
            case 0 -> executeKata(player, slot);
            case 1 -> executeSeishin(player, slot);
            default -> executeHakai(player, slot);
        }
    }

    private static void executeKata(ServerPlayer player, int slot) {
        switch (slot) {
            case 0 -> kataKyoka(player);
            case 1 -> crushingHand(player);
            default -> reinforcementCombo(player);
        }
    }

    private static void executeSeishin(ServerPlayer player, int slot) {
        switch (slot) {
            case 0 -> seishinDan(player);
            case 1 -> spiritualMachineGun(player);
            default -> spiritualIntangibility(player);
        }
    }

    private static void executeHakai(ServerPlayer player, int slot) {
        switch (slot) {
            case 0 -> explosivePunch(player);
            case 1 -> flameBarrages(player);
            default -> totalDestruction(player);
        }
    }

    private static void kataKyoka(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 2, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1, false, true));
        player.sendSystemMessage(Component.literal("Kata Kyōka 体強化 ativado! Defesa, força e velocidade aumentadas."));
    }

    private static void crushingHand(ServerPlayer player) {
        LivingEntity target = findTarget(player, 5.0D);
        if (target == null) { player.sendSystemMessage(Component.literal("Mão esmagadora errou.")); return; }
        target.setDeltaMovement(0, 0, 0);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 10, false, true));
        target.hurt(player.damageSources().playerAttack(player), 7.0F + player.getData(ModAttachments.PLAYER_DATA).strength());
        player.sendSystemMessage(Component.literal("Mão esmagadora! O alvo foi imobilizado."));
    }

    private static void reinforcementCombo(ServerPlayer player) {
        LivingEntity target = findTarget(player, 5.0D);
        if (target == null) return;
        int strength = player.getData(ModAttachments.PLAYER_DATA).strength();
        for (int i = 0; i < 6 && target.isAlive(); i++) target.hurt(player.damageSources().playerAttack(player), 3.0F + strength * 0.5F);
        player.sendSystemMessage(Component.literal("Combo de reforço!"));
    }

    private static void seishinDan(ServerPlayer player) {
        LivingEntity target = findTarget(player, 24.0D);
        int spiritual = player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment();
        if (target != null) target.hurt(player.damageSources().magic(), 3.0F + spiritual * 1.5F);
        player.sendSystemMessage(Component.literal("Tiro espiritual!"));
    }

    private static void spiritualMachineGun(ServerPlayer player) {
        int spiritual = player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment();
        int hits = 8;
        for (int i = 0; i < hits; i++) {
            LivingEntity target = findTarget(player, 24.0D);
            if (target == null) break;
            target.hurt(player.damageSources().magic(), 4.0F + spiritual * 1.25F);
        }
        player.sendSystemMessage(Component.literal("Metralhadora espiritual!"));
    }

    private static void spiritualIntangibility(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1, false, true));
        player.sendSystemMessage(Component.literal("Intangibilidade espiritual ativada por 5 segundos!"));
    }

    private static void explosivePunch(ServerPlayer player) {
        LivingEntity target = findTarget(player, 5.0D);
        if (target == null) return;
        target.hurt(player.damageSources().playerAttack(player), 8.0F);
        var center = target.position();
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.0D), e -> e != player && e.isAlive())) {
            entity.hurt(player.damageSources().playerAttack(player), 4.0F);
            var push = entity.position().subtract(center).normalize().scale(1.4D);
            entity.setDeltaMovement(push.x, 0.7D, push.z);
        }
        target.setDeltaMovement(0, 0.9D, 0);
        player.sendSystemMessage(Component.literal("Soco explosivo!"));
    }

    private static void flameBarrages(ServerPlayer player) {
        LivingEntity target = findTarget(player, 5.0D);
        if (target == null) return;
        int spiritual = player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment();
        for (int i = 0; i < 8 && target.isAlive(); i++) {
            target.hurt(player.damageSources().playerAttack(player), 3.0F + spiritual * 0.5F);
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 60));
        }
        player.sendSystemMessage(Component.literal("Barragem de golpes da chama da luta!"));
    }

    private static void totalDestruction(ServerPlayer player) {
        LivingEntity target = findTarget(player, 5.0D);
        if (target == null) return;
        target.hurt(player.damageSources().playerAttack(player), 70.0F);
        if (target.isAlive()) target.setHealth(0.0F);
        player.sendSystemMessage(Component.literal("Destruição total!"));
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
        var eye = player.getEyePosition(); var end = eye.add(player.getLookAngle().scale(range));
        var box = player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1.0D);
        LivingEntity best = null; double bestDistance = range * range;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
            var hit = entity.getBoundingBox().inflate(0.3D).clip(eye, end);
            if (hit.isPresent()) { double distance = eye.distanceToSqr(hit.get()); if (distance < bestDistance) { bestDistance = distance; best = entity; } }
        }
        return best;
    }

    private static int techniqueIndex(String technique) {
        if (technique == null) return 0;
        for (int i = 0; i < TECHNIQUES.length; i++) if (TECHNIQUES[i].equals(technique)) return i;
        return 0;
    }
}
