package br.mevis.kencraft.client;

import net.minecraft.client.Minecraft;

public final class KenCraftGuideClient {
    private KenCraftGuideClient() {}

    public static void open() {
        Minecraft.getInstance().setScreen(new KenCraftGuideScreen());
    }
}
