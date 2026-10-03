package br.mevis.kencraft.client;
import br.mevis.kencraft.data.ArfMissionData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.menu.AkioDialogueMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class AkioDialogueScreen extends AbstractContainerScreen<AkioDialogueMenu>{
 private static final int WIDTH=420,HEIGHT=252;
 public AkioDialogueScreen(AkioDialogueMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=WIDTH;imageHeight=HEIGHT;}
 @Override protected void init(){
  super.init();int left=(width-WIDTH)/2,top=(height-HEIGHT)/2;
  PlayerData data=Minecraft.getInstance().player.getData(ModAttachments.PLAYER_DATA);
  ArfMissionData mission=Minecraft.getInstance().player.getData(ModAttachments.ARF_MISSION);
  boolean talentUnlocked=data.race()==Race.HUMAN&&data.arfClass()!=0&&mission.mission1Completed();
  addRenderableWidget(Button.builder(Component.literal("Missões da ARF"),b->press(0)).bounds(left+25,top+166,175,24).build());
  addRenderableWidget(Button.builder(Component.literal("Conversar / Clã"),b->press(1)).bounds(left+220,top+166,175,24).build());
  Button talent=Button.builder(Component.literal("Talentos"),b->press(2)).bounds(left+25,top+196,175,24).build();
  talent.active=talentUnlocked;addRenderableWidget(talent);
  addRenderableWidget(Button.builder(Component.literal("Fechar"),b->Minecraft.getInstance().player.closeContainer()).bounds(left+220,top+196,175,24).build());
 }
 private void press(int id){Minecraft mc=Minecraft.getInstance();if(mc.gameMode!=null)mc.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
  int left=(width-WIDTH)/2,top=(height-HEIGHT)/2;
  g.fill(0,0,width,height,0x99000000);g.fill(left,top,left+WIDTH,top+HEIGHT,0xF01B1B22);
  g.fill(left+10,top+10,left+WIDTH-10,top+48,0xFF303642);g.fill(left+10,top+58,left+WIDTH-10,top+152,0xCC11141A);
  g.drawCenteredString(font,Component.literal("AKIO GINSHŌ"),width/2,top+20,0xFFFFFFFF);
  g.drawCenteredString(font,Component.literal("General da ARF"),width/2,top+34,0xFFFFCC66);
  g.drawString(font,Component.literal("Olá. O que deseja fazer?"),left+24,top+68,0xFFFFFFFF);
  g.drawString(font,Component.literal("• Missões: progresso e tarefas da ARF."),left+24,top+88,0xFFD0D5DD);
  g.drawString(font,Component.literal("• Clã: revelar ou consultar seu clã."),left+24,top+106,0xFFD0D5DD);
  g.drawString(font,Component.literal("• Talentos: liberado após a primeira missão."),left+24,top+124,0xFFD0D5DD);
  g.drawString(font,Component.literal("Reputação: "+mission.reputation()+"/200"),left+24,top+143,0xFFFFD966);
  g.drawString(font,Component.literal("Estágio: "+mission.stage()+" — "+mission.rankName()),left+225,top+143,0xFF9ED0FF);
 }
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
 @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g,mx,my,partial);super.render(g,mx,my,partial);}
}