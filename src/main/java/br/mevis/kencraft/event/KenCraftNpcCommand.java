package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.entity.ArfGeneralEntity;
import br.mevis.kencraft.entity.ArfInvestigatorEntity;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.RinkaEntity;
import br.mevis.kencraft.entity.RishinEntity;
import br.mevis.kencraft.item.KenCraftItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcCommand {
    public static final String ARF_NAME = "Investigador da ARF";
    public static final String ARF_GENERAL_NAME = "Investigador ARF General";
    public static final String RINKA_NAME = "Rinka";
    public static final String RISHIN_NAME = "Rishin";

    private KenCraftNpcCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("kencraft")
                .then(Commands.literal("npcs").executes(context -> spawnNpcs(context.getSource())))
                .then(Commands.literal("give")
                        .then(Commands.literal("jinsuikaku").executes(context -> giveJinsuikaku(context.getSource()))))
                .then(Commands.literal("jio")
                        .then(Commands.literal("charge").executes(context -> chargeJio(context.getSource())))
                        .then(Commands.literal("random").executes(context -> randomJioTechnique(context.getSource()))))
                .then(Commands.literal("kikan")
                        .then(Commands.literal("random").executes(context -> randomKikan(context.getSource())))
                        .then(Commands.literal("attack")
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .executes(context -> kikanAttack(context.getSource(), StringArgumentType.getString(context, "key")))))));
    }

    private static int spawnNpcs(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Esse comando precisa ser usado por um jogador."));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        BlockPos base = player.blockPosition();
        RinkaEntity rinka = KenCraftEntities.RINKA.get().create(level);
        ArfInvestigatorEntity arf = KenCraftEntities.ARF_INVESTIGATOR.get().create(level);
        ArfGeneralEntity general = KenCraftEntities.ARF_GENERAL.get().create(level);
        RishinEntity rishin = KenCraftEntities.RISHIN.get().create(level);
        if (rinka == null || arf == null || general == null || rishin == null) return 0;

        rinka.moveTo(base.offset(-4, 0, 0), 0, 0);
        rinka.finalizeSpawn(level, level.getCurrentDifficultyAt(rinka.blockPosition()), MobSpawnType.COMMAND, null);
        level.addFreshEntity(rinka);
        arf.moveTo(base.offset(4, 0, 0), 0, 0);
        arf.finalizeSpawn(level, level.getCurrentDifficultyAt(arf.blockPosition()), MobSpawnType.COMMAND, null);
        level.addFreshEntity(arf);
        general.moveTo(base.offset(0, 0, 4), 0, 0);
        general.finalizeSpawn(level, level.getCurrentDifficultyAt(general.blockPosition()), MobSpawnType.COMMAND, null);
        level.addFreshEntity(general);
        rishin.moveTo(base.offset(0, 0, -4), 0, 0);
        rishin.finalizeSpawn(level, level.getCurrentDifficultyAt(rishin.blockPosition()), MobSpawnType.COMMAND, null);
        level.addFreshEntity(rishin);
        source.sendSuccess(() -> Component.literal("KenCraft: Rinka, Investigador da ARF, General da ARF e Rishin criados."), true);
        return 1;
    }

    private static int giveJinsuikaku(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        player.getInventory().placeItemBackInInventory(new ItemStack(KenCraftItems.JINSUIKAKU.get()));
        source.sendSuccess(() -> Component.literal("KenCraft: Jinsuikaku adicionada ao inventário."), false);
        return 1;
    }

    private static int chargeJio(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN || data.arfClass() < 4) {
            source.sendFailure(Component.literal("Você precisa ser humano e ter entrado na ARF para aprender a controlar Jio."));
            return 0;
        }
        int max = ClanSystem.maxJio(player, data);
        int next = Math.min(max, data.jio() + 2);
        if (next == data.jio()) return 0;
        player.setData(ModAttachments.PLAYER_DATA, data.withJio(next, max));
        return 1;
    }

    private static int randomJioTechnique(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN || data.arfClass() < 4) {
            source.sendFailure(Component.literal("Você precisa entrar para a ARF antes de girar uma técnica Jio."));
            return 0;
        }
        String[] choices = {"REFORCO", "RAJADA", "BARREIRA"};
        String chosen = choices[player.getRandom().nextInt(choices.length)];
        player.setData(ModAttachments.PLAYER_DATA, data.withJioTechnique(chosen));
        source.sendSuccess(() -> Component.literal("Sua técnica Jio foi definida como: " + prettyJioTechnique(chosen)), false);
        return 1;
    }

    private static int randomKikan(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.RINKA || !data.canUseKikan()) {
            source.sendFailure(Component.literal("A Kikan só pode ser girada por um Rinka Classe C ou superior."));
            return 0;
        }
        String[] choices = {"CROCODILE_TAIL", "TENTACLE", "SCORPION_TAIL"};
        String chosen = choices[player.getRandom().nextInt(choices.length)];
        player.setData(ModAttachments.PLAYER_DATA, data.withKikanType(chosen));
        source.sendSuccess(() -> Component.literal("Sua Kikan foi definida como: " + prettyKikan(chosen)), false);
        return 1;
    }

    private static int kikanAttack(CommandSourceStack source, String key) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (!data.canUseKikan() || "NONE".equals(data.kikanType())) {
            source.sendFailure(Component.literal("Você precisa ser Classe C+ e girar uma Kikan primeiro."));
            return 0;
        }
        String attackKey = key.toLowerCase();
        if (!attackKey.equals("z") && !attackKey.equals("c")) return 0;

        List<LivingEntity> targets = player.serverLevel().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(4.0D), entity -> entity != player && entity.isAlive());
        LivingEntity target = targets.stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
        if (target == null) {
            player.sendSystemMessage(Component.literal("Kikan " + attackKey.toUpperCase() + ": nenhum alvo próximo."));
            return 0;
        }

        float damage = attackKey.equals("z") ? 6.0F : 9.0F;
        target.hurt(player.damageSources().playerAttack(player), damage);
        switch (data.kikanType()) {
            case "TENTACLE" -> target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, attackKey.equals("c") ? 1 : 0));
            case "SCORPION_TAIL" -> target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, attackKey.equals("c") ? 100 : 60, 0));
            case "CROCODILE_TAIL" -> target.setDeltaMovement(target.getDeltaMovement().add(player.getLookAngle().scale(0.45D)));
            default -> {}
        }
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        return 1;
    }

    private static String prettyKikan(String type) {
        return switch (type) {
            case "CROCODILE_TAIL" -> "Cauda de crocodilo";
            case "TENTACLE" -> "Tentáculo";
            case "SCORPION_TAIL" -> "Cauda de escorpião";
            default -> "Nenhuma";
        };
    }

    private static String prettyJioTechnique(String type) {
        return switch (type) {
            case "REFORCO" -> "Reforço";
            case "RAJADA" -> "Rajada";
            case "BARREIRA" -> "Barreira";
            default -> "Nenhuma";
        };
    }
}
