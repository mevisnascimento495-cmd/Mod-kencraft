package br.mevis.kencraft.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Rishin: a hostile Rinka belonging to the secret organization used in ARF missions. */
public class RishinEntity extends RinkaEntity {
    public RishinEntity(EntityType<? extends RinkaEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
