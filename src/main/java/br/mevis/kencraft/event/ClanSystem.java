package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ClanData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ClanSystem {
    private static final String KIRISAI = "KIRISAI";
    private static final String HIKARI = "HIKARI";
    private static final String YAKUMORI = "YAKUMORI";
    private static final String[] CLANS = {KIRISAI, HIKARI, YAKUMORI};
    private static final ResourceLocation CLAN_LIFE_ID = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "clan_life");

    private ClanSystem() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;

        ClanData clan = player.getData(ModAttachments.CLAN_DATA);
        if (clan.rolling() && player.tickCount % 4 == 0) {
            int nextTicks = clan.rollTicks() + 4;
            if (nextTicks >= 40) {
                String selected = CLANS[player.getRandom().nextInt(CLANS.length)];
                player.setData(ModAttachments.CLAN_DATA, clan.assign(selected));
                player.displayClientMessage(Component.literal("Seu clã foi revelado: " + displayName(selected) + "!"), true);
                player.sendSystemMessage(Component.literal("Clã: " + displayName(selected)));
                sendBonusSummary(player, selected);
            } else {
                ClanData next = new ClanData(clan.clan(), clan.readyToRoll(), true, nextTicks);
                player.setData(ModAttachments.CLAN_DATA, next);
                player.displayClientMessage(Component.literal("Clã: " + displayName(CLANS[player.getRandom().nextInt(CLANS.length)]) + "..."), true);
            }
        }

        if (player.tickCount % 5 == 0) applyLifeBonus(player);
        regenerateJio(player);
    }

    private static void applyLifeBonus(ServerPlayer player) {
        String clan = player.getData(ModAttachments.CLAN_DATA).clan();
        double bonus = switch (clan) {
            case KIRISAI -> 0.10D;
            case HIKARI -> 0.07D;
            case YAKUMORI -> 0.20D;
            default -> 0.0D;
        };
        var health = player.getAttributes().getInstance(Attributes.MAX_HEALTH);
        health.addOrUpdateTransientModifier(new AttributeModifier(CLAN_LIFE_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void regenerateJio(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN || data.maxJio() <= 0) return;
        double speed = jioRegenMultiplier(player);
        int interval = Math.max(1, (int)Math.round(20.0D / speed));
        if (player.tickCount % interval != 0) return;
        int max = maxJio(player, data);
        if (data.jio() < max) player.setData(ModAttachments.PLAYER_DATA, data.withJio(Math.min(max, data.jio() + 1), max));
    }

    public static int maxJio(ServerPlayer player, PlayerData data) {
        int base = data.calculatedHumanMaxJio();
        return (int)Math.round(base * jioStockMultiplier(player));
    }

    public static double jioRegenMultiplier(ServerPlayer player) {
        return switch (player.getData(ModAttachments.CLAN_DATA).clan()) {
            case KIRISAI -> 1.05D;
            case YAKUMORI -> 1.30D;
            default -> 1.0D;
        };
    }

    public static double jioStockMultiplier(ServerPlayer player) {
        return switch (player.getData(ModAttachments.CLAN_DATA).clan()) {
            case KIRISAI -> 1.05D;
            case YAKUMORI -> 1.30D;
            default -> 1.0D;
        };
    }

    public static double strengthMultiplier(ServerPlayer player) {
        return HIKARI.equals(player.getData(ModAttachments.CLAN_DATA).clan()) ? 1.03D : 1.0D;
    }

    public static double defenseMultiplier(ServerPlayer player) {
        if (HIKARI.equals(player.getData(ModAttachments.CLAN_DATA).clan())) {
            PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
            return "The King of Lies".equals(PlayerData.normalizeTechnique(data.jioTechnique())) ? 1.20D : 1.10D;
        }
        return YAKUMORI.equals(player.getData(ModAttachments.CLAN_DATA).clan()) ? 1.20D : 1.0D;
    }

    /** Reserved for the future God Thunder roll system. */
    public static double godThunderChanceBonus(ServerPlayer player) {
        return YAKUMORI.equals(player.getData(ModAttachments.CLAN_DATA).clan()) ? 0.20D : 0.0D;
    }

    public static boolean isKnownClan(String clan) {
        return KIRISAI.equals(clan) || HIKARI.equals(clan) || YAKUMORI.equals(clan);
    }

    public static String displayName(String clan) {
        return switch (clan) {
            case KIRISAI -> "Kirisai";
            case HIKARI -> "Hikari";
            case YAKUMORI -> "Yakumori";
            default -> "???";
        };
    }

    private static void sendBonusSummary(ServerPlayer player, String clan) {
        switch (clan) {
            case KIRISAI -> player.sendSystemMessage(Component.literal("Kirisai: +5% regeneração de Jio, +5% estoque máximo de Jio e +10% de vida."));
            case HIKARI -> player.sendSystemMessage(Component.literal("Hikari: +3% força, +7% vida e +10% defesa natural."));
            case YAKUMORI -> player.sendSystemMessage(Component.literal("Yakumori: +30% regeneração de Jio, +30% estoque máximo de Jio, +20% defesa natural. A chance de God Thunder será adicionada futuramente."));
            default -> {}
        }
    }
}
