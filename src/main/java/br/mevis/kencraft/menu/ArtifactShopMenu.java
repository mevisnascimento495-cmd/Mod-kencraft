package br.mevis.kencraft.menu;

import br.mevis.kencraft.item.KenCraftItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ArtifactShopMenu extends AbstractContainerMenu {
    private static final int[] PRICES = {8, 12, 16, 24};

    public ArtifactShopMenu(int id, Inventory inv) { this(id, inv, KenCraftMenus.ARTIFACT_SHOP.get()); }
    private ArtifactShopMenu(int id, Inventory inv, MenuType<?> type) { super(type, id); }

    @Override public boolean stillValid(Player player) { return true; }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer sp) || id < 0 || id >= 4) return false;
        Item item = switch (id) {
            case 0 -> KenCraftItems.SPIRIT_NECKLACE.get();
            case 1 -> KenCraftItems.VITALITY_RING.get();
            case 2 -> KenCraftItems.PERCEPTION_AMULET.get();
            default -> KenCraftItems.PARADISE_FRAGMENT.get();
        };
        int price = PRICES[id];
        if (!takeEmeralds(sp, price)) {
            sp.sendSystemMessage(Component.literal("§cVocê precisa de " + price + " esmeraldas."));
            return false;
        }
        sp.getInventory().placeItemBackInInventory(new ItemStack(item));
        sp.sendSystemMessage(Component.literal("§aCompra concluída: " + item.getName(new ItemStack(item)).getString() + "."));
        return true;
    }

    private static boolean takeEmeralds(ServerPlayer player, int amount) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) if (stack.is(Items.EMERALD)) total += stack.getCount();
        if (total < amount) return false;
        int remaining = amount;
        for (int i = 0; i < player.getInventory().items.size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.is(Items.EMERALD)) continue;
            int take = Math.min(remaining, stack.getCount());
            player.getInventory().removeItem(i, take);
            remaining -= take;
        }
        return true;
    }
}
