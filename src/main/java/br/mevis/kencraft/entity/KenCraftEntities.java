package br.mevis.kencraft.entity;

import br.mevis.kencraft.KenCraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class KenCraftEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, KenCraft.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<RinkaEntity>> RINKA = ENTITY_TYPES.register("rinka", () -> EntityType.Builder.of(RinkaEntity::new, MobCategory.MONSTER).sized(0.6F,1.8F).build("kencraft:rinka"));
    public static final DeferredHolder<EntityType<?>, EntityType<RankCRinkaEntity>> RANK_C_RINKA = ENTITY_TYPES.register("rank_c_rinka", () -> EntityType.Builder.of(RankCRinkaEntity::new, MobCategory.MONSTER).sized(0.7F,1.9F).build("kencraft:rank_c_rinka"));
    public static final DeferredHolder<EntityType<?>, EntityType<RishinEntity>> RISHIN = ENTITY_TYPES.register("rishin", () -> EntityType.Builder.of(RishinEntity::new, MobCategory.MONSTER).sized(0.6F,1.8F).build("kencraft:rishin"));
    public static final DeferredHolder<EntityType<?>, EntityType<AodaiEntity>> AODAI = ENTITY_TYPES.register("aodai", () -> EntityType.Builder.of(AodaiEntity::new, MobCategory.CREATURE).sized(0.6F,1.95F).build("kencraft:aodai"));
    public static final DeferredHolder<EntityType<?>, EntityType<ArfInvestigatorEntity>> ARF_INVESTIGATOR = ENTITY_TYPES.register("arf_investigator", () -> EntityType.Builder.of(ArfInvestigatorEntity::new, MobCategory.CREATURE).sized(0.6F,1.8F).build("kencraft:arf_investigator"));
    public static final DeferredHolder<EntityType<?>, EntityType<ArfGeneralEntity>> ARF_GENERAL = ENTITY_TYPES.register("arf_general", () -> EntityType.Builder.of(ArfGeneralEntity::new, MobCategory.CREATURE).sized(0.6F,1.8F).build("kencraft:arf_general"));
    public static final DeferredHolder<EntityType<?>, EntityType<HomareEntity>> SHIN_HOMARE = ENTITY_TYPES.register("shin_homare", () -> EntityType.Builder.of(HomareEntity::new, MobCategory.CREATURE).sized(0.6F,1.8F).build("kencraft:shin_homare"));
    public static final DeferredHolder<EntityType<?>, EntityType<HomareEntity>> KAORI_HOMARE = ENTITY_TYPES.register("kaori_homare", () -> EntityType.Builder.of(HomareEntity::new, MobCategory.CREATURE).sized(0.6F,1.8F).build("kencraft:kaori_homare"));

    private KenCraftEntities() {}

    public static final class HomareEntity extends PathfinderMob {
        public HomareEntity(EntityType<? extends PathfinderMob> type, net.minecraft.world.level.Level level) {
            super(type, level);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return PathfinderMob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 20.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.20D)
                    .add(Attributes.FOLLOW_RANGE, 16.0D);
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(1, new FloatGoal(this));
            this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D));
            this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
            this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        }
    }
}
