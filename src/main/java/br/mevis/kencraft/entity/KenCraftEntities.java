package br.mevis.kencraft.entity;

import br.mevis.kencraft.KenCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class KenCraftEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, KenCraft.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<RinkaEntity>> RINKA = ENTITY_TYPES.register("rinka",
            () -> EntityType.Builder.of(RinkaEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build("kencraft:rinka"));

    public static final DeferredHolder<EntityType<?>, EntityType<ArfInvestigatorEntity>> ARF_INVESTIGATOR = ENTITY_TYPES.register("arf_investigator",
            () -> EntityType.Builder.of(ArfInvestigatorEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build("kencraft:arf_investigator"));

    private KenCraftEntities() {}
}
