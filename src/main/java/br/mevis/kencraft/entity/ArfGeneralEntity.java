package br.mevis.kencraft.entity;

import br.mevis.kencraft.data.ClanData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.event.ClanSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
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
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 24.0D).add(Attributes.ARMOR, 6.0D).add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.95D, true));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player.level().isClientSide) return InteractionResult.SUCCESS;

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        boolean authorized = data.race() == Race.HUMAN && data.arfClass() > 0;
        ClanData clan = player.getData(ModAttachments.CLAN_DATA);

        if (!authorized) {
            player.sendSystemMessage(Component.literal("General Akio Ginshō: Você não pertence à ARF. Saia daqui antes que eu considere você uma ameaça."));
            this.setTarget(player);
            return InteractionResult.CONSUME;
        }

        if (clan.hasClan()) {
            player.sendSystemMessage(Component.literal("General Akio Ginshō: Seu clã já foi revelado. Você pertence ao clã " + ClanSystem.displayName(clan.clan()) + "."));
            return InteractionResult.CONSUME;
        }

        if (clan.rolling()) {
            player.sendSystemMessage(Component.literal("General Akio Ginshō: Aguarde. A técnica ainda está revelando seu clã..."));
            return InteractionResult.CONSUME;
        }

        if (!clan.readyToRoll()) {
            player.setData(ModAttachments.CLAN_DATA, clan.prepare());
            player.sendSystemMessage(Component.literal("General Akio Ginshō: Olá jogador, vejo que você nasceu em um clã, só não sabe qual é, certo? Vou usar minha técnica para revelar seu clã, seu clã apareça na sua mente, se prepare."));
            player.sendSystemMessage(Component.literal("Clique novamente no General para revelar seu clã."));
            return InteractionResult.CONSUME;
        }

        player.setData(ModAttachments.CLAN_DATA, clan.startRoll());
        player.sendSystemMessage(Component.literal("General Akio Ginshō: A revelação começou..."));
        return InteractionResult.CONSUME;
    }
}
