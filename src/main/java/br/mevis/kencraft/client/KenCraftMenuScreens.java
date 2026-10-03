package br.mevis.kencraft.client;
import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.menu.KenCraftMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
@EventBusSubscriber(modid=KenCraft.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class KenCraftMenuScreens{
 private KenCraftMenuScreens(){}
 @SubscribeEvent public static void register(RegisterMenuScreensEvent event){
  event.register(KenCraftMenus.AKIO_DIALOGUE.get(),AkioDialogueScreen::new);
  event.register(KenCraftMenus.ARTIFACT_SHOP.get(),ArtifactShopScreen::new);
 }
}