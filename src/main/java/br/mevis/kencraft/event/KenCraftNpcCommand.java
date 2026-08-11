package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LeatherArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcCommand {
    public static final String ARF_NAME = "Investigador da ARF";
    public static final String RINKA_NAME = "Rinka";

    private KenCraftNpcCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("kencraft")
                .then(Commands.literal("npcs")
                        .executes(context -> spawnNpcs(context.getSource()))));
    }

    private static int spawnNpcs(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Esse comando precisa ser usado por um jogador."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        BlockPos base = player.blockPosition();
        spawnNpc(level, base.offset(2, 0, 0), ARF_NAME, false);
        spawnNpc(level, base.offset(-2, 0, 0), RINKA_NAME, true);
        source.sendSuccess(() -> Component.literal("KenCraft: Investigador da ARF e Rinka criados. Use clique direito neles para receber XP."), true);
        return 1;
    }

    private static void spawnNpc(ServerLevel level, BlockPos pos, String displayName, boolean rinka) {
        Villager npc = EntityType.VILLAGER.spawn(level, pos, MobSpawnType.COMMAND);
        if (npc == null) return;

        npc.setCustomName(Component.literal(displayName));
        npc.setCustomNameVisible(true);
        npc.setNoAi(true);
        npc.setInvulnerable(true);
        npc.setSilent(true);
        npc.setPersistenceRequired();

        if (rinka) {
            npc.setItemSlot(EquipmentSlot.CHEST, dyed(Items.LEATHER_CHESTPLATE, 0x7A0E16));
            npc.setItemSlot(EquipmentSlot.LEGS, dyed(Items.LEATHER_LEGGINGS, 0x4A090D));
            npc.setItemSlot(EquipmentSlot.FEET, dyed(Items.LEATHER_BOOTS, 0x5E0B11));
        } else {
            npc.setItemSlot(EquipmentSlot.CHEST, dyed(Items.LEATHER_CHESTPLATE, DyeColor.WHITE.getTextureDiffuseColor()));
            npc.setItemSlot(EquipmentSlot.LEGS, dyed(Items.LEATHER_LEGGINGS, DyeColor.BLACK.getTextureDiffuseColor()));
            npc.setItemSlot(EquipmentSlot.FEET, dyed(Items.LEATHER_BOOTS, DyeColor.WHITE.getTextureDiffuseColor()));
        }
    }

    private static ItemStack dyed(net.minecraft.world.item.Item item, int color) {
        ItemStack stack = new ItemStack(item);
        if (stack.getItem() instanceof LeatherArmorItem) {
            LeatherArmorItem.setColor(stack, color);
        }
        return stack;
    }
}
