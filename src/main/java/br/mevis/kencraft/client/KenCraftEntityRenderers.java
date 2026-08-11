package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.ArfInvestigatorEntity;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.RinkaEntity;
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
        event.registerEntityRenderer(KenCraftEntities.RINKA.get(), ctx -> new SteveMobRenderer<>(ctx, 0.5F));
        event.registerEntityRenderer(KenCraftEntities.ARF_INVESTIGATOR.get(), ctx -> new SteveMobRenderer<>(ctx, 0.5F));
    }

    private static final class SteveMobRenderer<T extends net.minecraft.world.entity.LivingEntity>
            extends HumanoidMobRenderer<T, HumanoidModel<T>> {
        private SteveMobRenderer(EntityRendererProvider.Context context, float shadow) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), shadow);
        }

        @Override
        public ResourceLocation getTextureLocation(T entity) {
            return STEVE_TEXTURE;
        }
    }
}
