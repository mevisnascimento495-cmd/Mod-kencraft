package br.mevis.kencraft.menu;
import br.mevis.kencraft.event.ArfMissionSystem;
import br.mevis.kencraft.entity.AkioGinshoEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
public final class AkioDialogueMenu extends AbstractContainerMenu{
 public AkioDialogueMenu(int id,Inventory inv){this(id,inv,KenCraftMenus.AKIO_DIALOGUE.get());}
 private AkioDialogueMenu(int id,Inventory inv,MenuType<?> type){super(type,id);}
 @Override public boolean stillValid(Player player){return true;}
 @Override public boolean clickMenuButton(Player player,int id){
  if(!(player instanceof net.minecraft.server.level.ServerPlayer sp))return false;
  if(id==0){ArfMissionSystem.acceptOrShowMission(sp);return true;}
  if(id==1){AkioGinshoEntity.handleClanDialogue(sp);return true;}
  if(id==2){sp.closeContainer();return true;}
  return false;
 }
}