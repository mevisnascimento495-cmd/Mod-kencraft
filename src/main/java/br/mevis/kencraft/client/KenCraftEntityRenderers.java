package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.ArfGeneralEntity;
import br.mevis.kencraft.entity.ArfInvestigatorEntity;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.RinkaEntity;
import br.mevis.kencraft.entity.RishinEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class KenCraftEntityRenderers {
    private static final ResourceLocation STEVE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    private KenCraftEntityRenderers() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(KenCraftEntities.RINKA.get(), RinkaRenderer::new);
        event.registerEntityRenderer(KenCraftEntities.RISHIN.get(), RishinRenderer::new);
        event.registerEntityRenderer(KenCraftEntities.ARF_INVESTIGATOR.get(), ArfRenderer::new);
        event.registerEntityRenderer(KenCraftEntities.ARF_GENERAL.get(), GeneralRenderer::new);
    }

    private static final class RinkaRenderer extends HumanoidMobRenderer<RinkaEntity, HumanoidModel<RinkaEntity>> {
        private RinkaRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(RinkaEntity entity) {
            return STEVE_TEXTURE;
        }
    }

    private static final class RishinRenderer extends HumanoidMobRenderer<RishinEntity, HumanoidModel<RishinEntity>> {
        private RishinRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(RishinEntity entity) {
            return STEVE_TEXTURE;
        }
    }

    private static final class ArfRenderer extends HumanoidMobRenderer<ArfInvestigatorEntity, HumanoidModel<ArfInvestigatorEntity>> {
        private ArfRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(ArfInvestigatorEntity entity) {
            return STEVE_TEXTURE;
        }
    }

    private static final class GeneralRenderer extends HumanoidMobRenderer<ArfGeneralEntity, HumanoidModel<ArfGeneralEntity>> {
        private GeneralRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(ArfGeneralEntity entity) {
            return STEVE_TEXTURE;
        }
    }
}
