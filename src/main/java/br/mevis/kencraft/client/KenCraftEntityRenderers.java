package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid=KenCraft.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class KenCraftEntityRenderers {
    public static final ModelLayerLocation KIKAN_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"kikan"),"main");
    private static final ResourceLocation RINKA_TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/rinka.png");
    private static final ResourceLocation RISHIN_TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/rishin_generated.png");
    private static final ResourceLocation AODAI_TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/aodai.png");
    private static final ResourceLocation INTERIOR_SPIRIT_TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/interior_spirit.png");
    private KenCraftEntityRenderers() {}
    @SubscribeEvent public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(KenCraftEntities.RINKA.get(), RinkaRenderer::new);
        e.registerEntityRenderer(KenCraftEntities.RANK_C_RINKA.get(), RankCRinkaRenderer::new);
        e.registerEntityRenderer(KenCraftEntities.RISHIN.get(), RishinRenderer::new);
        e.registerEntityRenderer(KenCraftEntities.AODAI.get(), AodaiRenderer::new);
        e.registerEntityRenderer(KenCraftEntities.ARF_INVESTIGATOR.get(), ArfRenderer::new);
        e.registerEntityRenderer(KenCraftEntities.ARF_GENERAL.get(), GeneralRenderer::new);
        e.registerEntityRenderer(KenCraftEntities.INTERIOR_SPIRIT.get(), InteriorSpiritRenderer::new);
    }
    @SubscribeEvent public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(KIKAN_LAYER, KikanModel::createBodyLayer);
        e.registerLayerDefinition(AodaiSnakeModel.LAYER, AodaiSnakeModel::createBodyLayer);
    }
    @SubscribeEvent public static void addPlayerLayers(EntityRenderersEvent.AddLayers e) {
        for(PlayerSkin.Model skin:e.getSkins()) if(e.getSkin(skin) instanceof PlayerRenderer pr){ pr.addLayer(new KikanLayer(pr,e.getEntityModels())); pr.addLayer(new JioAuraLayer(pr,e.getEntityModels())); }
    }
    private static HumanoidModel<RinkaEntity> playerModel(EntityRendererProvider.Context c){ return new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)); }
    private static HumanoidModel<InteriorSpiritEntity> spiritModel(EntityRendererProvider.Context c){ return new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)); }
    private static final class RinkaRenderer extends HumanoidMobRenderer<RinkaEntity,HumanoidModel<RinkaEntity>> { RinkaRenderer(EntityRendererProvider.Context c){super(c,playerModel(c),0.5F);} public ResourceLocation getTextureLocation(RinkaEntity e){return RINKA_TEXTURE;} }
    private static final class RankCRinkaRenderer extends HumanoidMobRenderer<RankCRinkaEntity,HumanoidModel<RankCRinkaEntity>> { RankCRinkaRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),0.5F);} public ResourceLocation getTextureLocation(RankCRinkaEntity e){return RINKA_TEXTURE;} }
    private static final class RishinRenderer extends HumanoidMobRenderer<RishinEntity,HumanoidModel<RishinEntity>> { RishinRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),0.5F); this.model.hat.visible=false;} public ResourceLocation getTextureLocation(RishinEntity e){return RISHIN_TEXTURE;} }
    private static final class AodaiRenderer extends HumanoidMobRenderer<AodaiEntity,HumanoidModel<AodaiEntity>> { AodaiRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),0.5F); this.model.hat.visible=false; addLayer(new AodaiSnakeLayer(this,c));} public ResourceLocation getTextureLocation(AodaiEntity e){return AODAI_TEXTURE;} }
    private static final class ArfRenderer extends HumanoidMobRenderer<ArfInvestigatorEntity,HumanoidModel<ArfInvestigatorEntity>> { ArfRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),0.5F);} public ResourceLocation getTextureLocation(ArfInvestigatorEntity e){return ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/arf.png");} }
    private static final class GeneralRenderer extends HumanoidMobRenderer<ArfGeneralEntity,HumanoidModel<ArfGeneralEntity>> { GeneralRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),0.5F);} public ResourceLocation getTextureLocation(ArfGeneralEntity e){return ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/arf.png");} }
    private static final class InteriorSpiritRenderer extends HumanoidMobRenderer<InteriorSpiritEntity,HumanoidModel<InteriorSpiritEntity>> { InteriorSpiritRenderer(EntityRendererProvider.Context c){super(c,spiritModel(c),0.5F);} public ResourceLocation getTextureLocation(InteriorSpiritEntity e){return INTERIOR_SPIRIT_TEXTURE;} }
}