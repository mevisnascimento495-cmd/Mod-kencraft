package br.mevis.kencraft.entity;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
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
        return PathfinderMob.createMobAttributes()
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

        if (data.arfClass() >= 4) {
            player.sendSystemMessage(Component.literal("General da ARF: Você já é um investigador de quarta classe."));
            return InteractionResult.CONSUME;
        }

        if (data.arfMissionKills() < 0) {
            player.setData(ModAttachments.PLAYER_DATA, data.withArfMissionKills(0));
            sendMission(player);
            return InteractionResult.CONSUME;
        }

        if (data.arfMissionKills() >= 5) {
            player.setData(ModAttachments.PLAYER_DATA, data.withArfClass(4));
            player.sendSystemMessage(Component.literal("Excelente jogador(a), você eliminou 5 Rinkas. Agora você é um investigador de quarta classe. Bem-vindo à ARF."));
            return InteractionResult.CONSUME;
        }

        player.sendSystemMessage(Component.literal("General da ARF: Você já iniciou a missão. Rinkas derrotados: " + data.arfMissionKills() + "/5."));
        return InteractionResult.CONSUME;
    }

    private static void sendMission(Player player) {
        player.sendSystemMessage(Component.literal("Olá jogador(a), vejo que vc quer se tornar parte da ARF e aprender a controlar Jio, você precisa matar 5 Rinkas, depois disso volte até mim vou te tornar um investigador de quarta classe"));
    }
}
