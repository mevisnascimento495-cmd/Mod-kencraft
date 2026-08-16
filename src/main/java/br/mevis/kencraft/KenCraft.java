package br.mevis.kencraft;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.entity.*;
import br.mevis.kencraft.event.ChatSelectionHandler;
import br.mevis.kencraft.event.PlayerLoginHandler;
import br.mevis.kencraft.item.KenCraftItems;
import br.mevis.kencraft.world.MinamoriStructureGenerator;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(KenCraft.MOD_ID)
public class KenCraft {
    public static final String MOD_ID = "kencraft";
    public KenCraft(IEventBus modEventBus) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        KenCraftEntities.ENTITY_TYPES.register(modEventBus);
        KenCraftItems.ARMOR_MATERIALS.register(modEventBus);
        KenCraftItems.ITEMS.register(modEventBus);
        NeoForge.EVENT_BUS.register(PlayerLoginHandler.class);
        NeoForge.EVENT_BUS.register(ChatSelectionHandler.class);
        NeoForge.EVENT_BUS.register(MinamoriStructureGenerator.class);
    }
    @EventBusSubscriber(modid=MOD_ID,bus=EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent public static void createAttributes(EntityAttributeCreationEvent event) {
            event.put(KenCraftEntities.RINKA.get(), RinkaEntity.createAttributes().build());
            event.put(KenCraftEntities.RANK_C_RINKA.get(), RankCRinkaEntity.createAttributes().build());
            event.put(KenCraftEntities.RISHIN.get(), RishinEntity.createAttributes().build());
            event.put(KenCraftEntities.AODAI.get(), AodaiEntity.createAttributes().build());
            event.put(KenCraftEntities.ARF_INVESTIGATOR.get(), ArfInvestigatorEntity.createAttributes().build());
            event.put(KenCraftEntities.ARF_GENERAL.get(), ArfGeneralEntity.createAttributes().build());
            event.put(KenCraftEntities.SHIN_HOMARE.get(), KenCraftEntities.HomareEntity.createAttributes().build());
            event.put(KenCraftEntities.KAORI_HOMARE.get(), KenCraftEntities.HomareEntity.createAttributes().build());
        }
        @SubscribeEvent public static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
            if (CreativeModeTabs.INGREDIENTS.equals(event.getTabKey())) {
                event.accept(KenCraftItems.JINSUIKAKU.get());
                event.accept(KenCraftItems.JINSUIKAKU_RANK_C.get());
            }
            if (CreativeModeTabs.SPAWN_EGGS.equals(event.getTabKey())) {
                event.accept(KenCraftItems.RINKA_SPAWN_EGG.get());
                event.accept(KenCraftItems.RANK_C_RINKA_SPAWN_EGG.get());
                event.accept(KenCraftItems.RISHIN_SPAWN_EGG.get());
                event.accept(KenCraftItems.AODAI_SPAWN_EGG.get());
                event.accept(KenCraftItems.ARF_INVESTIGATOR_SPAWN_EGG.get());
                event.accept(KenCraftItems.ARF_GENERAL_SPAWN_EGG.get());
                event.accept(KenCraftItems.SHIN_HOMARE_SPAWN_EGG.get());
                event.accept(KenCraftItems.KAORI_HOMARE_SPAWN_EGG.get());
            }
            if (CreativeModeTabs.COMBAT.equals(event.getTabKey())) {
                event.accept(KenCraftItems.ARF_UNIFORM_CHESTPLATE.get());
                event.accept(KenCraftItems.ARF_UNIFORM_LEGGINGS.get());
            }
        }
    }
}
