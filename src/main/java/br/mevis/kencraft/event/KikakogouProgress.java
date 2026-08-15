package br.mevis.kencraft.event;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class KikakogouProgress {
    private static final String MISSION = "kencraft_kikakogou_mission";
    private static final String DEFEATED = "kencraft_kikakogou_aodai_defeated";
    private static final String UNLOCKED = "kencraft_kikakogou_unlocked";
    private KikakogouProgress() {}
    public static boolean isMissionStarted(ServerPlayer p) { return p.getPersistentData().getBoolean(MISSION); }
    public static boolean isAodaiDefeated(ServerPlayer p) { return p.getPersistentData().getBoolean(DEFEATED); }
    public static boolean isUnlocked(ServerPlayer p) { return p.getPersistentData().getBoolean(UNLOCKED); }
    public static void startMission(ServerPlayer p) { p.getPersistentData().putBoolean(MISSION, true); }
    public static void markAodaiDefeated(ServerPlayer p) { if (p.getData(ModAttachments.PLAYER_DATA).race() != Race.RINKA || !isMissionStarted(p)) return; p.getPersistentData().putBoolean(DEFEATED, true); tryUnlock(p, true); }
    public static void tryUnlockFromConsumption(ServerPlayer p) { tryUnlock(p, true); }
    private static boolean tryUnlock(ServerPlayer p, boolean announce) {
        if (isUnlocked(p) || !isMissionStarted(p) || !isAodaiDefeated(p)) return isUnlocked(p);
        PlayerData data = p.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.RINKA || data.jinsuikakuConsumed() < 50 || data.jinsuikakuRankCConsumed() < 30) return false;
        p.getPersistentData().putBoolean(UNLOCKED, true);
        if (announce) p.sendSystemMessage(Component.literal("Parabéns! Você completou a missão de Aodai e desbloqueou a Kikakogou. Use X para usar a Kikakogou."));
        return true;
    }
}
