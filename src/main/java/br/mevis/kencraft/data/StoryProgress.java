package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;

/** Persistent story progress for the KenCraft narrative. */
public record StoryProgress(int stage) {
    public static final StoryProgress DEFAULT = new StoryProgress(0);
    public static final Codec<StoryProgress> CODEC = Codec.INT.xmap(StoryProgress::new, StoryProgress::stage);

    public StoryProgress withStage(int nextStage) {
        return new StoryProgress(Math.max(0, nextStage));
    }
}
