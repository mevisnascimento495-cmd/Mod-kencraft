package br.mevis.kencraft.item;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KenCraftItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KenCraft.MOD_ID);

    public static final DeferredHolder<Item, JinsuikakuItem> JINSUIKAKU = ITEMS.register("jinsuikaku",
            () -> new JinsuikakuItem(new Item.Properties().stacksTo(16).food(
                    new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(4).saturationModifier(0.4F).alwaysEdible().build())));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> RINKA_SPAWN_EGG = ITEMS.register("rinka_spawn_egg",
            () -> new DeferredSpawnEggItem(KenCraftEntities.RINKA, 0x241C1C, 0x8B1E2D, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> RANK_C_RINKA_SPAWN_EGG = ITEMS.register("rank_c_rinka_spawn_egg",
            () -> new DeferredSpawnEggItem(KenCraftEntities.RANK_C_RINKA, 0x160D16, 0xB52B3B, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> ARF_INVESTIGATOR_SPAWN_EGG = ITEMS.register("arf_investigator_spawn_egg",
            () -> new DeferredSpawnEggItem(KenCraftEntities.ARF_INVESTIGATOR, 0xF2F2F2, 0x242424, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> ARF_GENERAL_SPAWN_EGG = ITEMS.register("arf_general_spawn_egg",
            () -> new DeferredSpawnEggItem(KenCraftEntities.ARF_GENERAL, 0xE8E8E8, 0x111111, new Item.Properties()));

    private KenCraftItems() {}
}
