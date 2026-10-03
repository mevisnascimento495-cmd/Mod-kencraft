package br.mevis.kencraft.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public final class KatanaComumItem extends SwordItem {
    public KatanaComumItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(SwordItem.createAttributes(Tiers.IRON, 6, -2.4F)));
    }
}
