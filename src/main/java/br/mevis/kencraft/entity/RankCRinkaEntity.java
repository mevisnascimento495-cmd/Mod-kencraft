package br.mevis.kencraft.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** Rank C Rinka: an elite aggressive Rinka with a random Kikan type. */
public class RankCRinkaEntity extends RinkaEntity {
    private static final String[] KIKAN_TYPES = {"CROCODILE_TAIL", "TENTACLE", "SCORPION_TAIL"};
    private String kikanType = "CROCODILE_TAIL";

    public RankCRinkaEntity(EntityType<? extends RinkaEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.kikanType = KIKAN_TYPES[this.random.nextInt(KIKAN_TYPES.length)];
    }

    public static AttributeSupplier.Builder createAttributes() {
        return RinkaEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 70.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 11.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D);
    }

    public String getKikanType() {
        return kikanType;
    }
}
