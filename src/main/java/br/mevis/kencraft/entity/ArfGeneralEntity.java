package br.mevis.kencraft.entity;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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
                .add(Attributes.ARMOR, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.level().isClientSide) return InteractionResult.SUCCESS;

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN) {
            player.sendSystemMessage(Component.literal("O General da ARF só recruta humanos."));
            return InteractionResult.CONSUME;
        }

        if (data.arfClass() == 0) {
            if (data.arfMissionKills() < 0) {
                player.setData(ModAttachments.PLAYER_DATA, data.withArfMissionKills(0));
                sendRecruitmentMission(player);
                return InteractionResult.CONSUME;
            }
            if (data.arfMissionKills() >= 5) {
                player.setData(ModAttachments.PLAYER_DATA, data.withArfClass(4).withArfMissionKills(0));
                player.sendSystemMessage(Component.literal("Você eliminou 5 Rinkas. Você entrou para a ARF como Investigador de Quarta Classe!"));
                player.sendSystemMessage(Component.literal("Próxima missão: elimine 5 Rishins da organização secreta e volte ao General."));
                return InteractionResult.CONSUME;
            }
            player.sendSystemMessage(Component.literal("Missão de recrutamento: Rinkas derrotados " + data.arfMissionKills() + "/5."));
            return InteractionResult.CONSUME;
        }

        int required = requiredRishins(data.arfClass());
        if (data.arfClass() > 1 && data.arfMissionKills() >= required) {
            int newRank = data.arfClass() - 1;
            player.setData(ModAttachments.PLAYER_DATA, data.withArfClass(newRank).withArfMissionKills(0));
            player.sendSystemMessage(Component.literal("Parabéns! Você foi promovido para Investigador de " + rankName(newRank) + "."));
            if (newRank > 1) {
                player.sendSystemMessage(Component.literal("Próxima missão: elimine " + requiredRishins(newRank) + " Rishins e volte ao General."));
            } else {
                player.sendSystemMessage(Component.literal("Você alcançou o Rank 1 da ARF. Novas missões especiais serão desbloqueadas em breve."));
            }
            return InteractionResult.CONSUME;
        }

        if (data.arfClass() > 1) {
            player.sendSystemMessage(Component.literal("Missão ARF Rank " + data.arfClass() + ": Rishins derrotados " + Math.max(0, data.arfMissionKills()) + "/" + required + "."));
        } else {
            player.sendSystemMessage(Component.literal("Você já alcançou o Rank 1 da ARF."));
        }
        return InteractionResult.CONSUME;
    }

    private static int requiredRishins(int arfClass) {
        return switch (arfClass) {
            case 4 -> 5;
            case 3 -> 10;
            case 2 -> 20;
            default -> 0;
        };
    }

    private static int requiredRishins(int newRank) {
        return switch (newRank) {
            case 3 -> 10;
            case 2 -> 20;
            default -> 0;
        };
    }

    private static String rankName(int rank) {
        return switch (rank) {
            case 4 -> "Quarta Classe";
            case 3 -> "Terceira Classe";
            case 2 -> "Segunda Classe";
            case 1 -> "Primeira Classe (Rank 1)";
            default -> "Sem Rank";
        };
    }

    private static void sendRecruitmentMission(Player player) {
        player.sendSystemMessage(Component.literal("Olá jogador(a), vejo que vc quer se tornar parte da ARF e aprender a controlar Jio, você precisa matar 5 Rinkas, depois disso volte até mim vou te tornar um investigador de quarta classe"));
    }
}
