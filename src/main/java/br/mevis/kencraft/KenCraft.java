package br.mevis.kencraft;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.entity.ArfInvestigatorEntity;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.RinkaEntity;
import br.mevis.kencraft.event.ChatSelectionHandler;
import br.mevis.kencraft.event.PlayerLoginHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(KenCraft.MOD_ID)
public class KenCraft {
    public static final String MOD_ID = "kencraft";

    public KenCraft(IEventBus modEventBus) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        KenCraftEntities.ENTITY_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.register(PlayerLoginHandler.class);
        NeoForge.EVENT_BUS.register(ChatSelectionHandler.class);
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {}

        @SubscribeEvent
        public static void createAttributes(EntityAttributeCreationEvent event) {
            event.put(KenCraftEntities.RINKA.get(), RinkaEntity.createAttributes().build());
            event.put(KenCraftEntities.ARF_INVESTIGATOR.get(), ArfInvestigatorEntity.createAttributes().build());
        }
    }
}
