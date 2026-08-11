package br.mevis.kencraft.entity;

import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;

public class ArfInvestigatorEntity extends PathfinderMob {
    public ArfInvestigatorEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setItemSlot(EquipmentSlot.CHEST, dyed(Items.LEATHER_CHESTPLATE, DyeColor.WHITE));
        this.setItemSlot(EquipmentSlot.LEGS, dyed(Items.LEATHER_LEGGINGS, DyeColor.WHITE));
        this.setItemSlot(EquipmentSlot.FEET, dyed(Items.LEATHER_BOOTS, DyeColor.BLACK));
    }

    private static ItemStack dyed(net.minecraft.world.item.Item item, DyeColor color) {
        ItemStack stack = new ItemStack(item);
        return DyedItemColor.applyDyes(stack, List.of(DyeItem.byColor(color)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ARMOR, 3.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }
}
