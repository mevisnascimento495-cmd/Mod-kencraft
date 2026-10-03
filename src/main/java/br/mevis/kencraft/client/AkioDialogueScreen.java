package br.mevis.kencraft.client;
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
 private static final int WIDTH=390,HEIGHT=230;
 public AkioDialogueScreen(AkioDialogueMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=WIDTH;imageHeight=HEIGHT;}
 @Override protected void init(){super.init();int l=(width-WIDTH)/2,t=(height-HEIGHT)/2;PlayerData d=Minecraft.getInstance().player.getData(ModAttachments.PLAYER_DATA);boolean arf=d.race()==Race.HUMAN&&d.arfClass()!=0;if(arf)addRenderableWidget(Button.builder(Component.literal("Missões da ARF"),b->press(0)).bounds(l+35,t+164,150,24).build());addRenderableWidget(Button.builder(Component.literal("Conversar / Clã"),b->press(1)).bounds(l+205,t+164,150,24).build());addRenderableWidget(Button.builder(Component.literal("Fechar"),b->Minecraft.getInstance().player.closeContainer()).bounds(l+145,t+196,100,20).build());}
 private void press(int id){Minecraft mc=Minecraft.getInstance();if(mc.gameMode!=null)mc.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 @Override protected void renderBg(GuiGraphics g,float partial,float mx,float my){int l=(width-WIDTH)/2,t=(height-HEIGHT)/2;g.fill(0,0,width,height,0x99000000);g.fill(l,t,l+WIDTH,t+HEIGHT,0xF01B1B22);g.fill(l+10,t+10,l+WIDTH-10,t+48,0xFF303642);g.fill(l+10,t+58,l+WIDTH-10,t+150,0xCC11141A);g.drawCenteredString(font,Component.literal("AKIO GINSHŌ"),width/2,t+22,0xFFFFFFFF);g.drawString(font,Component.literal("General da ARF"),l+24,t+66,0xFFFFCC66);g.drawString(font,Component.literal("Olá. O que deseja fazer?"),l+24,t+88,0xFFFFFFFF);g.drawString(font,Component.literal("Posso revelar seu clã ou apresentar as missões."),l+24,t+108,0xFFD0D5DD);g.drawString(font,Component.literal("Escolha uma opção abaixo."),l+24,t+128,0xFFD0D5DD);}
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
 @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g,mx,my,partial);super.render(g,mx,my,partial);}
}