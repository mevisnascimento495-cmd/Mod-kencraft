package br.mevis.kencraft;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.event.ChatSelectionHandler;
import br.mevis.kencraft.event.PlayerLoginHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(KenCraft.MOD_ID)
public class KenCraft {
    public static final String MOD_ID = "kencraft";

    public KenCraft(IEventBus modEventBus) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.register(PlayerLoginHandler.class);
        NeoForge.EVENT_BUS.register(ChatSelectionHandler.class);
    }
}
