package br.mevis.kencraft.entity;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ArfGeneralEntity extends PathfinderMob {
    public ArfGeneralEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.95D, true));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Generals actively defend the ARF's mission area by prioritizing Rinkas.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, RinkaEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        LivingEntity target = getTarget();
        if (target == null) return;

        if (!target.isAlive() || target.isRemoved()) {
            setTarget(null);
            return;
        }

        double followRange = getAttributeValue(Attributes.FOLLOW_RANGE);
        if (distanceToSqr(target) > followRange * followRange * 2.25D) {
            setTarget(null);
            return;
        }

        // A general coordinates nearby ARF members around an active Rinka threat.
        if (target instanceof RinkaEntity && tickCount % 10 == 0) {
            for (ArfInvestigatorEntity investigator : level().getEntitiesOfClass(
                    ArfInvestigatorEntity.class, getBoundingBox().inflate(16.0D),
                    entity -> entity.isAlive())) {
                investigator.setTarget(target);
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player.level().isClientSide) return InteractionResult.SUCCESS;

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN) {
            player.sendSystemMessage(Component.literal("General ARF: Você não pertence à ARF. Saia daqui antes que eu considere você uma ameaça."));
            this.setTarget(player);
            return InteractionResult.CONSUME;
        }

        if (data.arfClass() == 0) {
            if (data.arfMissionKills() < 0) {
                player.setData(ModAttachments.PLAYER_DATA, data.withArfMissionKills(0));
                player.sendSystemMessage(Component.literal("Olá jogador(a), vejo que vc quer se tornar parte da ARF e aprender a controlar Jio, você precisa matar 5 Rinkas, depois disso volte até mim vou te tornar um investigador de quarta classe"));
                return InteractionResult.CONSUME;
            }
            if (data.arfMissionKills() >= 5) {
                player.setData(ModAttachments.PLAYER_DATA, data.withArfClass(4).withArfMissionKills(0));
                player.sendSystemMessage(Component.literal("Você eliminou 5 Rinkas. Você entrou para a ARF como Investigador de Quarta Classe!"));
                player.sendSystemMessage(Component.literal("Próxima missão: elimine 3 Rinkas Rank C e volte ao General."));
                return InteractionResult.CONSUME;
            }
            player.sendSystemMessage(Component.literal("Missão de recrutamento: Rinkas derrotados " + data.arfMissionKills() + "/5."));
            return InteractionResult.CONSUME;
        }

        // Mission 5: the First Class investigator must defeat one Rank B Rinka.
        if (data.arfClass() == 1) {
            if (data.arfMissionKills() >= 1) {
                player.sendSystemMessage(Component.literal("Missão 5/5 concluída! Você derrotou o Rinka Rank B."));
                player.sendSystemMessage(Component.literal("Você concluiu as cinco missões do General da ARF e mantém a patente de Primeira Classe."));
            } else {
                player.sendSystemMessage(Component.literal("Missão 5/5 — Primeira Classe: derrote 1 Rinka Rank B e volte ao General."));
                player.sendSystemMessage(Component.literal("Progresso: " + Math.max(0, data.arfMissionKills()) + "/1 Rinka Rank B."));
            }
            return InteractionResult.CONSUME;
        }

        int required = requiredRankCRinkas(data.arfClass());
        if (required > 0 && data.arfMissionKills() >= required) {
            int newRank = data.arfClass() - 1;
            player.setData(ModAttachments.PLAYER_DATA, data.withArfClass(newRank).withArfMissionKills(0));
            player.sendSystemMessage(Component.literal("Missão concluída! Promoção para Investigador de " + rankName(newRank) + "."));
            if (newRank == 3) {
                player.sendSystemMessage(Component.literal("Missão 3/5 — Terceira Classe: derrote 6 Rinkas Rank C e volte ao General."));
                player.sendSystemMessage(Component.literal("Progresso da próxima missão: 0/6."));
            } else if (newRank == 2) {
                player.sendSystemMessage(Component.literal("Missão 4/5 — Segunda Classe: derrote 10 Rinkas Rank C e volte ao General."));
                player.sendSystemMessage(Component.literal("Progresso da próxima missão: 0/10."));
            } else if (newRank == 1) {
                player.sendSystemMessage(Component.literal("Missão 5/5 — Primeira Classe: derrote 1 Rinka Rank B e volte ao General."));
                player.sendSystemMessage(Component.literal("Progresso da próxima missão: 0/1."));
            }
            return InteractionResult.CONSUME;
        }

        if (required > 0) {
            player.sendSystemMessage(Component.literal("Missão " + missionNumber(data.arfClass()) + "/5 — " + rankName(data.arfClass())));
            player.sendSystemMessage(Component.literal("Derrote " + required + " Rinkas Rank C e volte ao General."));
            player.sendSystemMessage(Component.literal("Progresso: " + Math.max(0, data.arfMissionKills()) + "/" + required + " Rinkas Rank C."));
            return InteractionResult.CONSUME;
        }

        player.sendSystemMessage(Component.literal("General ARF: não foi possível identificar sua missão atual. Fale novamente comigo."));
        return InteractionResult.CONSUME;
    }

    public static int requiredRankCRinkas(int arfClass) {
        return switch (arfClass) {
            case 4 -> 3;
            case 3 -> 6;
            case 2 -> 10;
            default -> 0;
        };
    }

    private static int missionNumber(int arfClass) {
        return switch (arfClass) {
            case 4 -> 2;
            case 3 -> 3;
            case 2 -> 4;
            case 1 -> 5;
            default -> 1;
        };
    }

    @SubscribeEvent
    public static void onHigherRankArfMissionKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;

        var victim = event.getEntity();
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        String path = key == null ? "" : key.getPath().toLowerCase(java.util.Locale.ROOT);
        boolean rankBRinka = victim instanceof RinkaRankBAkiraEntity || (key != null
                && KenCraft.MOD_ID.equals(key.getNamespace())
                && path.contains("rinka")
                && (path.contains("rank_b") || path.contains("rankb") || path.contains("rank-b")));

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN || data.arfMissionKills() < 0) return;

        if (rankBRinka && data.arfClass() == 1 && data.arfMissionKills() < 1) {
            player.setData(ModAttachments.PLAYER_DATA, data.withArfMissionKills(1));
            player.sendSystemMessage(Component.literal("Missão ARF 5/5: Rinka Rank B derrotado! Volte ao General para concluir."));
            return;
        }

        // KenCraftNpcRewards already counts Rank C kills for Fourth Class.
        // This handler completes the missing Third and Second Class tracking.
        if (victim instanceof RankCRinkaEntity && (data.arfClass() == 3 || data.arfClass() == 2)) {
            int required = requiredRankCRinkas(data.arfClass());
            if (data.arfMissionKills() >= required) return;
            int kills = data.arfMissionKills() + 1;
            player.setData(ModAttachments.PLAYER_DATA, data.withArfMissionKills(kills));
            player.sendSystemMessage(Component.literal("Missão ARF " + missionNumber(data.arfClass())
                    + "/5: Rinkas Rank C derrotados " + kills + "/" + required + "."));
            if (kills >= required) {
                player.sendSystemMessage(Component.literal("Missão concluída! Volte ao General da ARF para receber sua promoção."));
            }
        }
    }

    public static String rankName(int rank) {
        return switch (rank) {
            case 4 -> "Quarta Classe";
            case 3 -> "Terceira Classe";
            case 2 -> "Segunda Classe";
            case 1 -> "Primeira Classe (Rank 1)";
            default -> "Sem Rank";
        };
    }
}
