package br.mevis.kencraft.world;

import br.mevis.kencraft.item.KenCraftItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.RandomSource;

public final class StructureLoot {
    private StructureLoot() {}
    public static void hospital(ServerLevel level, BlockPos... containers) {
        for (int i=0;i<containers.length;i++) fill(level,containers[i],
                stack(Items.BREAD,2+i), stack(Items.GOLDEN_APPLE,i==0?1:0),
                stack(KenCraftItems.JINSUIKAKU.get(),1+i));
    }
    public static void arf(ServerLevel level, BlockPos... containers) {
        for (int i=0;i<containers.length;i++) fill(level,containers[i],
                stack(Items.IRON_INGOT,3+i*2), stack(Items.ARROW,8+i*4),
                stack(KenCraftItems.JINSUIKAKU_RANK_C.get(),i==0?1:0),
                stack(KenCraftItems.ARF_UNIFORM_CHESTPLATE.get(),i==0?1:0),
                stack(KenCraftItems.KATANA_COMUM.get(),i==0 && RandomSource.create().nextFloat() < 0.30F ? 1 : 0));
    }
    public static void minamori(ServerLevel level, BlockPos... containers) {
        for (int i=0;i<containers.length;i++) fill(level,containers[i],
                stack(Items.EMERALD,2+i), stack(Items.BREAD,3),
                stack(KenCraftItems.JINSUIKAKU.get(),1+i%2),
                stack(KenCraftItems.PERCEPTION_AMULET.get(),i==0?1:0));
    }
    private static ItemStack stack(ItemLike item,int count){return count<=0?ItemStack.EMPTY:new ItemStack(item,count);}
    private static void fill(ServerLevel level,BlockPos pos,ItemStack... loot){
        if(!(level.getBlockEntity(pos) instanceof Container container)) return;
        for(int slot=0;slot<loot.length;slot++) if(!loot[slot].isEmpty()) container.setItem(slot,loot[slot]);
        if(level.getBlockEntity(pos)!=null) level.getBlockEntity(pos).setChanged();
    }
}
