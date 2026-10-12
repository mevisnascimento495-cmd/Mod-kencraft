package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ArfMissionData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.TalentData;
import br.mevis.kencraft.entity.RinkaHungryEntity;
import br.mevis.kencraft.entity.RishinEntity;
import br.mevis.kencraft.entity.KenkiSenkoEntity;
import br.mevis.kencraft.entity.MitsoEntity;
import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ArfMissionSystem {
    private ArfMissionSystem() {}

    public static void acceptOrShowMission(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN || data.arfClass() == 0) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: As missões da ARF são exclusivas para investigadores humanos."));
            return;
        }

        ArfMissionData mission = player.getData(ModAttachments.ARF_MISSION);
        if (mission.active()) {
            showCurrentTask(player, mission);
            return;
        }

        if (mission.allRegularMissionsCompleted()) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: Você concluiu as 30 missões regulares da ARF."));
            player.sendSystemMessage(Component.literal("Patente de missão: " + mission.rankName() + " | Reputação: " + mission.reputation() + "/200."));
            player.sendSystemMessage(Component.literal("A última etapa não possui outro Rank UP."));
            return;
        }

        if (mission.rankUpPending()) {
            player.setData(ModAttachments.ARF_MISSION, mission.startNextTask());
            ArfMissionData started = player.getData(ModAttachments.ARF_MISSION);
            player.sendSystemMessage(Component.literal("Akio Ginshō: Você concluiu 10 missões do estágio " + (started.stage()) + "."));
            player.sendSystemMessage(Component.literal("MISSÃO DE RANK UP — alcance o próximo estágio de treinamento."));
            player.sendSystemMessage(Component.literal("Esta missão de Rank UP usa o ID " + started.missionId() + "."));
            player.sendSystemMessage(Component.literal("Depois dela, novas missões do estágio " + (started.stage() + 1) + " serão liberadas."));
            return;
        }

        player.setData(ModAttachments.ARF_MISSION, mission.startNextTask());
        ArfMissionData started = player.getData(ModAttachments.ARF_MISSION);

        if (started.missionId() == 1) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: Tenho uma missão para você."));
            player.sendSystemMessage(Component.literal("Missão 1 — Distrito 7 → Distrito 1"));
            player.sendSystemMessage(Component.literal("Derrote o Rinka do distrito 7 que veio para o 1°. Ele está em um hospital abandonado."));
            player.sendSystemMessage(Component.literal("Alvo: Rinka Hungry. Recompensa: 100 de reputação."));
        } else if (started.missionId() == 2) {
            player.sendSystemMessage(Component.literal("Missão 2 — Investigação da Rishin: encontre o alçapão, explore o covil subterrâneo e derrote os três Rishin da sala central."));
        } else if (started.missionId() == 3) {
            player.sendSystemMessage(Component.literal("Missão 3 — Investigação da Rishin: encontre a sala de Kenki Senkō e derrote o membro superior de manto vermelho."));
        } else if (started.missionId() == 4) {
            player.sendSystemMessage(Component.literal("Missão 4 — Investigação da Rishin: depois de derrotar Kenki, saia do covil e enfrente Mitso e a emboscada."));
        } else if (started.missionId() == 5) {
            player.sendSystemMessage(Component.literal("Missão 5 — O desafio de Aodai."));
            player.sendSystemMessage(Component.literal("Akio Ginshō: Você concluiu as missões anteriores. Agora prove sua força enfrentando Aodai!"));
            spawnAodaiChallenge(player);
        } else {
            player.sendSystemMessage(Component.literal("ARF — Missão " + started.missionId() + " do estágio " + started.stage() + "."));
            player.sendSystemMessage(Component.literal("Objetivo desta missão ainda não foi definido no projeto."));
        }
        player.sendSystemMessage(Component.literal("Patente de missão: " + started.rankName() + " | Reputação: " + started.reputation() + "/200."));
    }

    private static void showCurrentTask(ServerPlayer player, ArfMissionData mission) {
        if (mission.missionId() == 1) {
            player.sendSystemMessage(Component.literal("ARF — Missão 1 ativa."));
            player.sendSystemMessage(Component.literal("Derrote o Rinka do distrito 7 que veio para o 1°. Ele está em um hospital abandonado."));
        } else if (mission.missionId() == 2) {
            player.sendSystemMessage(Component.literal("Missão 2 ativa: encontre o covil subterrâneo e derrote os três Rishin da sala central."));
            player.sendSystemMessage(Component.literal("Progresso: " + mission.rishinCentralKills() + "/3."));
        } else if (mission.missionId() == 3) {
            player.sendSystemMessage(Component.literal("Missão 3 ativa: encontre Kenki Senkō no canto do covil e derrote-o."));
        } else if (mission.missionId() == 4) {
            player.sendSystemMessage(Component.literal("Missão 4 ativa: saia do covil para iniciar a emboscada de Mitso."));
            if (mission.mitsoEventTriggered()) player.sendSystemMessage(Component.literal("Emboscada ativa: derrote Mitso."));
        } else if (mission.missionId() == 5) {
            player.sendSystemMessage(Component.literal("Missão 5 ativa: enfrente e derrote Aodai."));
        } else if (mission.missionId() == 11 || mission.missionId() == 22) {
            player.sendSystemMessage(Component.literal("ARF — MISSÃO DE RANK UP " + mission.missionId() + " ativa."));
            player.sendSystemMessage(Component.literal("Objetivo específico do Rank UP ainda não foi definido."));
        } else {
            player.sendSystemMessage(Component.literal("ARF — Missão " + mission.missionId() + " ativa."));
            player.sendSystemMessage(Component.literal("Objetivo específico ainda não foi definido."));
        }
        player.sendSystemMessage(Component.literal("Estágio: " + mission.stage() + " — " + mission.rankName() + " | Reputação: " + mission.reputation() + "/200."));
    }

    public static boolean canUseTalentRoulette(ServerPlayer player) {
        ArfMissionData mission = player.getData(ModAttachments.ARF_MISSION);
        return mission.mission1Completed();
    }

    public static void completeTalentUnlockCheck(ServerPlayer player) {
        if (canUseTalentRoulette(player)) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: A Roleta de Talentos está disponível para você."));
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        Entity victim = event.getEntity();
        ArfMissionData mission = player.getData(ModAttachments.ARF_MISSION);
        if (!mission.active()) return;

        if (victim instanceof RinkaHungryEntity && mission.missionId() == 1) {
            ArfMissionData done = mission.completeCurrentTask();
            player.setData(ModAttachments.ARF_MISSION, done);
            player.sendSystemMessage(Component.literal("Missão 1 concluída! Você derrotou Rinka Hungry."));
            player.sendSystemMessage(Component.literal("Recompensa recebida: +100 de reputação."));
            player.sendSystemMessage(Component.literal("Reputação atual: " + done.reputation() + "/200."));
            player.sendSystemMessage(Component.literal("Patente de missão da ARF: " + done.rankName() + "."));
            completeTalentUnlockCheck(player);
            return;
        }

        if (victim instanceof RishinEntity && victim.getPersistentData().getBoolean("kencraft_rishin_coven_central")
                && mission.missionId() == 2) {
            int kills = mission.rishinCentralKills() + 1;
            ArfMissionData updated = mission.withRishinCentralKills(kills);
            if (kills >= 3) {
                updated = updated.completeCurrentTask().withRishinCentralKills(0).startNextTask();
                player.sendSystemMessage(Component.literal("Missão 2 concluída! A sala central está limpa."));
                player.sendSystemMessage(Component.literal("Missão 3 iniciada: encontre e derrote Kenki Senkō."));
            } else {
                player.sendSystemMessage(Component.literal("Rishin centrais derrotados: " + kills + "/3."));
            }
            player.setData(ModAttachments.ARF_MISSION, updated);
            return;
        }

        if (victim instanceof KenkiSenkoEntity
                && victim.getPersistentData().getBoolean("kencraft_rishin_coven_kenki") && mission.missionId() == 3) {
            ArfMissionData next = mission.completeCurrentTask().startNextTask();
            player.setData(ModAttachments.ARF_MISSION, next);
            player.sendSystemMessage(Component.literal("Missão 3 concluída! Kenki Senkō foi derrotado."));
            player.sendSystemMessage(Component.literal("Missão 4 iniciada: saia do covil para enfrentar Mitso."));
            return;
        }

        if (victim instanceof MitsoEntity && victim.getPersistentData().getBoolean("kencraft_mitso_event")
                && mission.missionId() == 4
                && player.getUUID().toString().equals(victim.getPersistentData().getString("kencraft_mitso_owner"))) {
            player.setData(ModAttachments.ARF_MISSION, mission.completeCurrentTask().clearRishinInvestigation());
            player.sendSystemMessage(Component.literal("Missão 4 concluída! Mitso foi derrotado e a emboscada terminou."));
            player.sendSystemMessage(Component.literal("Você concluiu a Investigação da Rishin. Volte a falar com Akio para receber a próxima missão."));
            return;
        }

        if (victim instanceof br.mevis.kencraft.entity.AodaiEntity
                && victim.getPersistentData().getBoolean("kencraft_akio_mission_aodai")
                && mission.missionId() == 5
                && player.getUUID().toString().equals(victim.getPersistentData().getString("kencraft_aodai_owner"))) {
            ArfMissionData done = mission.completeCurrentTask();
            player.setData(ModAttachments.ARF_MISSION, done);
            player.sendSystemMessage(Component.literal("Missão 5 concluída! Você derrotou Aodai."));
            player.sendSystemMessage(Component.literal("Recompensa recebida: +" + Math.max(0, done.reputation() - mission.reputation()) + " de reputação."));
            player.sendSystemMessage(Component.literal("Volte a falar com Akio para continuar sua carreira na ARF."));
        }
    }

    private static void spawnAodaiChallenge(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Entity aodai = KenCraftEntities.AODAI.get().create(level);
        if (aodai == null) {
            player.sendSystemMessage(Component.literal("Não foi possível iniciar o desafio de Aodai. Tente falar com Akio novamente."));
            return;
        }
        aodai.moveTo(player.getX() + 3.5D, player.getY(), player.getZ() + 0.5D, player.getYRot(), 0.0F);
        aodai.setCustomName(Component.literal("Aodai — Desafio da ARF"));
        aodai.setCustomNameVisible(true);
        aodai.getPersistentData().putBoolean("kencraft_akio_mission_aodai", true);
        aodai.getPersistentData().putString("kencraft_aodai_owner", player.getUUID().toString());
        if (level.noCollision(aodai) && level.addFreshEntity(aodai)) {
            player.sendSystemMessage(Component.literal("Aodai foi convocado para o desafio. Prepare-se para lutar!"));
        } else {
            aodai.discard();
            player.sendSystemMessage(Component.literal("Não foi possível posicionar Aodai com segurança. Afaste-se de paredes e tente novamente com Akio."));
            player.setData(ModAttachments.ARF_MISSION, player.getData(ModAttachments.ARF_MISSION).completeCurrentTask());
        }
    }

    @SubscribeEvent
    public static void trackCovenEntryAndExit(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 5 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                ArfMissionData mission = player.getData(ModAttachments.ARF_MISSION);
                if (!mission.active() || mission.missionId() < 2 || mission.missionId() > 4) continue;
                if (!mission.rishinCovenEntered()) {
                    BlockPos hatch = findHatch(level, player);
                    if (hatch != null && player.getY() < hatch.getY() + 0.5D) {
                        player.setData(ModAttachments.ARF_MISSION,
                                mission.enteredRishinCoven(hatch.getX(), hatch.getY(), hatch.getZ()));
                    }
                    continue;
                }
                boolean atHatch = Math.abs(player.getX() - mission.covenEntryX() - 0.5D) < 1.5D
                        && Math.abs(player.getZ() - mission.covenEntryZ() - 0.5D) < 1.5D;
                if (!atHatch || player.getY() < mission.covenEntryY() + 1.0D) continue;
                if (mission.missionId() == 4 && !mission.mitsoEventTriggered()) {
                    spawnMitsoAmbush(level, player);
                    player.setData(ModAttachments.ARF_MISSION,
                            mission.exitedRishinCoven().withMitsoEventTriggered(true));
                } else {
                    player.setData(ModAttachments.ARF_MISSION, mission.exitedRishinCoven());
                }
            }
        }
    }

    private static BlockPos findHatch(ServerLevel level, ServerPlayer player) {
        int px = player.blockPosition().getX();
        int pz = player.blockPosition().getZ();
        int minY = Math.max(level.getMinBuildHeight(), player.blockPosition().getY() + 1);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, player.blockPosition().getY() + 64);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(px + dx, y, pz + dz);
                    if (level.getBlockState(pos).is(Blocks.CRIMSON_TRAPDOOR)) return pos.offset(0, 0, -1);
                }
            }
        }
        return null;
    }

    private static void spawnMitsoAmbush(ServerLevel level, ServerPlayer player) {
        player.sendSystemMessage(Component.literal(
                "Mitso: VOCÊ, VOCÊ ACHOU NOSSO ESCONDERIJO, DERROTOU MEUS SUBORDINADOS, E ACHA QUE VAI IR EMBORA AGORA?!"));
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI * 2.0D * i / 10.0D;
            int x = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * (5 + i % 2));
            int z = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * (5 + i % 2));
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            Entity mob = KenCraftEntities.RISHIN.get().create(level);
            if (mob == null) continue;
            mob.moveTo(x + 0.5D, y, z + 0.5D, i * 36.0F, 0.0F);
            mob.getPersistentData().putBoolean("kencraft_mitso_event_minion", true);
            if (level.noCollision(mob)) level.addFreshEntity(mob);
        }
        Entity mitso = KenCraftEntities.MITSO.get().create(level);
        if (mitso != null) {
            mitso.moveTo(player.getX() + 2.5D, player.getY(), player.getZ() + 2.5D,
                    player.getYRot(), 0.0F);
            mitso.setCustomName(Component.literal("Mitso"));
            mitso.setCustomNameVisible(true);
            mitso.getPersistentData().putBoolean("kencraft_mitso_event", true);
            mitso.getPersistentData().putString("kencraft_mitso_owner", player.getUUID().toString());
            if (level.noCollision(mitso)) level.addFreshEntity(mitso);
        }
    }

}