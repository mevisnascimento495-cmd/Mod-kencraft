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
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Named ARF general used by the ARF headquarters story: Akio Ginsho. */
public class AkioGinshoEntity extends ArfGeneralEntity {
    public AkioGinshoEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player.level().isClientSide) return InteractionResult.SUCCESS;

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);

        // Akio only accepts humans who are already members of the ARF.
        if (data.race() != Race.HUMAN || data.arfClass() == 0) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: Você não pertence à ARF. Saia daqui antes que eu considere você uma ameaça."));
            this.setTarget(player);
            return InteractionResult.CONSUME;
        }

        ClanData clan = player.getData(ModAttachments.CLAN_DATA);
        if (clan.hasClan()) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: Seu clã já foi revelado. Você pertence ao clã " + ClanSystem.displayName(clan.clan()) + "."));
            return InteractionResult.CONSUME;
        }

        if (clan.rolling()) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: Aguarde. A técnica ainda está revelando seu clã..."));
            return InteractionResult.CONSUME;
        }

        if (!clan.readyToRoll()) {
            player.setData(ModAttachments.CLAN_DATA, clan.prepare());
            player.sendSystemMessage(Component.literal("Olá jogador, vejo que você nasceu em um clã, só não sabe qual é, certo? Vou usar minha técnica para revelar seu clã, seu clã apareça na sua mente, se prepare"));
            player.sendSystemMessage(Component.literal("Clique novamente no Akio para revelar seu clã."));
            return InteractionResult.CONSUME;
        }

        player.setData(ModAttachments.CLAN_DATA, clan.startRoll());
        player.sendSystemMessage(Component.literal("Akio Ginshō: A revelação começou..."));
        return InteractionResult.CONSUME;
    }
}
