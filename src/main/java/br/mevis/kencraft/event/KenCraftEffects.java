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
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, KenCraft.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> SUFOCO = EFFECTS.register("sufoco", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x3B8EDB) {
        @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return duration > 0 && duration % 20 == 0; }
        @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) { entity.hurt(entity.damageSources().drown(), 2.0F + amplifier); return true; }
    });

    public static final DeferredHolder<MobEffect, MobEffect> QUEBRA_DE_MEMBROS = EFFECTS.register("quebra_de_membros", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x7A4A32) {
        @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return duration > 0; }
        @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) { entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,3,2,false,true,false));entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WEAKNESS,3,2,false,true,false));return true; }
    });

    /** The hallucination state already used by Kikakogou; exposed as a named effect for Jio states too. */
    public static final DeferredHolder<MobEffect, MobEffect> ALUCINACAO = EFFECTS.register("alucinacao", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x707070) {
        @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return duration > 0; }
        @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) { entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.CONFUSION,4,0,false,true,false));return true; }
    });

    /** Spiritual Jio effect: effectively maximum resistance and regeneration while active. */
    public static final DeferredHolder<MobEffect, MobEffect> IMORTALIDADE = EFFECTS.register("imortalidade", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF) {
        @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return duration > 0; }
        @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) { entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,3,255,false,false,false));entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.REGENERATION,3,255,false,false,false));return true; }
    });

    private KenCraftEffects() {}
}
