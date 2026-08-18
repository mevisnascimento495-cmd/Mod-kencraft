package br.mevis.kencraft.data;

import br.mevis.kencraft.KenCraft;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

public final class ModAttachments {
    private ModAttachments() {}
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, KenCraft.MOD_ID);
    public static final Supplier<AttachmentType<PlayerData>> PLAYER_DATA = ATTACHMENT_TYPES.register("player_data", () -> AttachmentType.builder(() -> PlayerData.DEFAULT).serialize(PlayerData.CODEC).sync(PlayerData.STREAM_CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<StoryProgress>> STORY_PROGRESS = ATTACHMENT_TYPES.register("story_progress", () -> AttachmentType.builder(() -> StoryProgress.DEFAULT).serialize(StoryProgress.CODEC).sync(StoryProgress.STREAM_CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<JioAnimationData>> JIO_ANIMATION = ATTACHMENT_TYPES.register("jio_animation", () -> AttachmentType.builder(() -> JioAnimationData.DEFAULT).serialize(JioAnimationData.CODEC).sync(JioAnimationData.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<KikakogouState>> KIKAKOGOU_STATE = ATTACHMENT_TYPES.register("kikakogou_state", () -> AttachmentType.builder(() -> KikakogouState.DEFAULT).serialize(KikakogouState.CODEC).sync(KikakogouState.STREAM_CODEC).copyOnDeath().build());
}
