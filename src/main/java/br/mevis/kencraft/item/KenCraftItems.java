package br.mevis.kencraft.item;

import br.mevis.kencraft.KenCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KenCraftItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KenCraft.MOD_ID);

    public static final DeferredHolder<Item, JinsuikakuItem> JINSUIKAKU = ITEMS.register("jinsuikaku",
            () -> new JinsuikakuItem(new Item.Properties().stacksTo(16).food(
                    new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.4F)
                            .alwaysEdible()
                            .build())));

    private KenCraftItems() {}
}
