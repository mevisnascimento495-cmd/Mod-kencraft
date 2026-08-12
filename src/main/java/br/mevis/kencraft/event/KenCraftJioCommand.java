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
                .then(Commands.literal("cycle").executes(c -> cycle(c.getSource())))
                .then(Commands.literal("attack").executes(c -> attack(c.getSource())))
                .then(Commands.literal("charge").executes(c -> charge(c.getSource())))));
    }

    private static boolean eligible(PlayerData data) { return data.race() == Race.HUMAN && data.arfClass() >= 4; }

    private static int random(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) { player.sendSystemMessage(Component.literal("Você precisa ser Investigador de Quarta Classe ou superior para usar Jio.")); return 0; }
        String chosen = TECHNIQUES[player.getRandom().nextInt(TECHNIQUES.length)];
        player.setData(ModAttachments.PLAYER_DATA, data.withJioTechnique(chosen));
        player.sendSystemMessage(Component.literal("Técnica Jio sorteada: " + chosen));
        return 1;
    }

    private static int cycle(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) { player.sendSystemMessage(Component.literal("Você precisa ser Investigador de Quarta Classe ou superior para usar Jio.")); return 0; }
        int next = (techniqueIndex(data.jioTechnique()) + 1) % TECHNIQUES.length;
        player.setData(ModAttachments.PLAYER_DATA, data.withJioTechnique(TECHNIQUES[next]));
        player.sendSystemMessage(Component.literal("Técnica Jio: " + TECHNIQUES[next]));
        return 1;
    }

    private static int attack(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!eligible(data)) { player.sendSystemMessage(Component.literal("Você ainda não possui Rank ARF suficiente para usar Jio.")); return 0; }
        if (data.jio() < 30) { player.sendSystemMessage(Component.literal("Jio insuficiente: cada técnica custa 30 Jio.")); return 0; }
        int current = techniqueIndex(data.jioTechnique());
        player.setData(ModAttachments.PLAYER_DATA, data.withJio(data.jio() - 30, data.calculatedHumanMaxJio()));
        switch (current) { case 0 -> kataKyoka(player); case 1 -> seishinDan(player); default -> seimeiKui(player); }
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

    private static void kataKyoka(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 12, 2, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 12, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 12, 1, false, true));
        player.sendSystemMessage(Component.literal("Kata Kyōka 体強化 ativado! Defesa, força e velocidade aumentadas."));
    }

    private static void seishinDan(ServerPlayer player) {
        LivingEntity target = findTarget(player, 24.0D);
        int spiritual = player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment();
        if (target != null) {
            target.hurt(player.damageSources().magic(), 3.0F + spiritual * 1.5F);
            player.sendSystemMessage(Component.literal("Seishin Dan 精神弾 atingiu o alvo!"));
        } else player.sendSystemMessage(Component.literal("Seishin Dan 精神弾 disparado, mas não atingiu um alvo."));
    }

    private static void seimeiKui(ServerPlayer player) {
        LivingEntity target = findTarget(player, 5.0D);
        int spiritual = player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment();
        if (target != null) {
            target.hurt(player.damageSources().playerAttack(player), 4.0F + spiritual);
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 40));
            for (LivingEntity nearby : player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.5D), e -> e != player && e != target && e.isAlive())) {
                nearby.hurt(player.damageSources().playerAttack(player), 2.0F + spiritual * 0.5F);
                nearby.setRemainingFireTicks(Math.max(nearby.getRemainingFireTicks(), 40));
            }
            player.sendSystemMessage(Component.literal("生命喰 Hakai Satsu Tōtetsu: Seimei Kui!"));
        }
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
