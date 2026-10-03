package br.mevis.kencraft.menu;
import br.mevis.kencraft.KenCraft;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
public final class KenCraftMenus{
 public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(net.minecraft.core.registries.Registries.MENU,KenCraft.MOD_ID);
 public static final DeferredHolder<MenuType<?>,MenuType<AkioDialogueMenu>> AKIO_DIALOGUE=MENUS.register("akio_dialogue",()->new MenuType<>(AkioDialogueMenu::new,net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
 private KenCraftMenus(){}
}