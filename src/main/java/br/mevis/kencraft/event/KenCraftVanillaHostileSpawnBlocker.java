package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftVanillaHostileSpawnBlocker {
    private KenCraftVanillaHostileSpawnBlocker() {}

    @SubscribeEvent
    public static void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL) return;
        EntityType<?> type = event.getEntityType();
        if (type.getCategory() != MobCategory.MONSTER) return;
        String namespace = BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace();
        if (KenCraft.MOD_ID.equals(namespace)) return;
        event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
    }
}
