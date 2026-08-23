package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KenCraftEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, KenCraft.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> SUFOCO =
            EFFECTS.register("sufoco", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x3B8EDB) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return duration > 0 && duration % 20 == 0;
                }

                @Override
                public boolean applyEffectTick(LivingEntity entity, int amplifier) {
                    entity.hurt(entity.damageSources().drown(), 2.0F + amplifier);
                    return true;
                }
            });

    public static final DeferredHolder<MobEffect, MobEffect> QUEBRA_DE_MEMBROS =
            EFFECTS.register("quebra_de_membros", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x7A4A32) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return duration > 0;
                }

                @Override
                public boolean applyEffectTick(LivingEntity entity, int amplifier) {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN, 3, 2, false, true, false));
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            MobEffects.WEAKNESS, 3, 2, false, true, false));
                    return true;
                }
            });

    private KenCraftEffects() {}
}
