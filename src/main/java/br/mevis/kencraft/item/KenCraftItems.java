package br.mevis.kencraft.item;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.Util;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;

public final class KenCraftItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KenCraft.MOD_ID);
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, KenCraft.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARF_UNIFORM_MATERIAL = ARMOR_MATERIALS.register("arf_uniform", () -> new ArmorMaterial(
            Util.make(new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 0);
                map.put(ArmorItem.Type.CHESTPLATE, 3);
                map.put(ArmorItem.Type.LEGGINGS, 3);
                map.put(ArmorItem.Type.BOOTS, 0);
                map.put(ArmorItem.Type.BODY, 0);
            }),
            10,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(Items.WHITE_WOOL),
            java.util.List.of(new ArmorMaterial.Layer(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "arf_uniform"))),
            0.0F,
            0.0F));

    public static final DeferredHolder<Item, ArmorItem> ARF_UNIFORM_CHESTPLATE = ITEMS.register("arf_uniform_chestplate", () -> new ArmorItem(ARF_UNIFORM_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(15))));
    public static final DeferredHolder<Item, ArmorItem> ARF_UNIFORM_LEGGINGS = ITEMS.register("arf_uniform_leggings", () -> new ArmorItem(ARF_UNIFORM_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(15))));

    public static final DeferredHolder<Item,JinsuikakuItem> JINSUIKAKU = ITEMS.register("jinsuikaku", () -> new JinsuikakuItem(new Item.Properties().stacksTo(16).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(4).saturationModifier(0.4F).alwaysEdible().build())));
    public static final DeferredHolder<Item,JinsuikakuRankCItem> JINSUIKAKU_RANK_C = ITEMS.register("jinsuikaku_rank_c", () -> new JinsuikakuRankCItem(new Item.Properties().stacksTo(16).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationModifier(0.6F).alwaysEdible().build())));
    public static final DeferredHolder<Item,DeferredSpawnEggItem> RINKA_SPAWN_EGG = ITEMS.register("rinka_spawn_egg", () -> new DeferredSpawnEggItem(KenCraftEntities.RINKA,0x241C1C,0x8B1E2D,new Item.Properties()));
    public static final DeferredHolder<Item,DeferredSpawnEggItem> RANK_C_RINKA_SPAWN_EGG = ITEMS.register("rank_c_rinka_spawn_egg", () -> new DeferredSpawnEggItem(KenCraftEntities.RANK_C_RINKA,0x160D16,0xB52B3B,new Item.Properties()));
    public static final DeferredHolder<Item,DeferredSpawnEggItem> RISHIN_SPAWN_EGG = ITEMS.register("rishin_spawn_egg", () -> new DeferredSpawnEggItem(KenCraftEntities.RISHIN,0x15151A,0x9B1825,new Item.Properties()));
    public static final DeferredHolder<Item,DeferredSpawnEggItem> AODAI_SPAWN_EGG = ITEMS.register("aodai_spawn_egg", () -> new DeferredSpawnEggItem(KenCraftEntities.AODAI,0xE8E8E8,0x1AAA62,new Item.Properties()));
    public static final DeferredHolder<Item,DeferredSpawnEggItem> ARF_INVESTIGATOR_SPAWN_EGG = ITEMS.register("arf_investigator_spawn_egg", () -> new DeferredSpawnEggItem(KenCraftEntities.ARF_INVESTIGATOR,0xF2F2F2,0x242424,new Item.Properties()));
    public static final DeferredHolder<Item,DeferredSpawnEggItem> ARF_GENERAL_SPAWN_EGG = ITEMS.register("arf_general_spawn_egg", () -> new DeferredSpawnEggItem(KenCraftEntities.ARF_GENERAL,0xE8E8E8,0x111111,new Item.Properties()));
    public static final DeferredHolder<Item,DeferredSpawnEggItem> SHIN_HOMARE_SPAWN_EGG = ITEMS.register("shin_homare_spawn_egg", () -> new DeferredSpawnEggItem(KenCraftEntities.SHIN_HOMARE,0xE6D5C5,0x3A2A23,new Item.Properties()));
    public static final DeferredHolder<Item,DeferredSpawnEggItem> KAORI_HOMARE_SPAWN_EGG = ITEMS.register("kaori_homare_spawn_egg", () -> new DeferredSpawnEggItem(KenCraftEntities.KAORI_HOMARE,0xF0D7C0,0x8B5A2B,new Item.Properties()));

    private KenCraftItems() {}
}
