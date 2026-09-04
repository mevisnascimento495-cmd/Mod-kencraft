package br.mevis.kencraft.entity;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.StoryProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KenCraftEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, KenCraft.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<RinkaEntity>> RINKA = ENTITY_TYPES.register("rinka", () -> EntityType.Builder.of(RinkaEntity::new, MobCategory.MONSTER).sized(0.6F,1.8F).build("kencraft:rinka"));
    public static final DeferredHolder<EntityType<?>, EntityType<RankCRinkaEntity>> RANK_C_RINKA = ENTITY_TYPES.register("rank_c_rinka", () -> EntityType.Builder.of(RankCRinkaEntity::new, MobCategory.MONSTER).sized(0.7F,1.9F).build("kencraft:rank_c_rinka"));
    public static final DeferredHolder<EntityType<?>, EntityType<RishinEntity>> RISHIN = ENTITY_TYPES.register("rishin", () -> EntityType.Builder.of(RishinEntity::new, MobCategory.MONSTER).sized(0.6F,1.8F).build("kencraft:rishin"));
    public static final DeferredHolder<EntityType<?>, EntityType<AodaiEntity>> AODAI = ENTITY_TYPES.register("aodai", () -> EntityType.Builder.of(AodaiEntity::new, MobCategory.CREATURE).sized(0.6F,1.95F).build("kencraft:aodai"));
    public static final DeferredHolder<EntityType<?>, EntityType<ArfInvestigatorEntity>> ARF_INVESTIGATOR = ENTITY_TYPES.register("arf_investigator", () -> EntityType.Builder.of(ArfInvestigatorEntity::new, MobCategory.CREATURE).sized(0.6F,1.8F).build("kencraft:arf_investigator"));
    public static final DeferredHolder<EntityType<?>, EntityType<ArfGeneralEntity>> ARF_GENERAL = ENTITY_TYPES.register("arf_general", () -> EntityType.Builder.of(ArfGeneralEntity::new, MobCategory.CREATURE).sized(0.6F,1.8F).build("kencraft:arf_general"));
    public static final DeferredHolder<EntityType<?>, EntityType<AkioGinshoEntity>> AKIO_GINSHO = ENTITY_TYPES.register("akio_ginsho", () -> EntityType.Builder.of(AkioGinshoEntity::new, MobCategory.CREATURE).sized(0.6F,1.8F).build("kencraft:akio_ginsho"));
    public static final DeferredHolder<EntityType<?>, EntityType<OnokiEntity>> ONOKI = ENTITY_TYPES.register("onoki", () -> EntityType.Builder.of(OnokiEntity::new, MobCategory.CREATURE).sized(0.6F,1.8F).build("kencraft:onoki"));
    public static final DeferredHolder<EntityType<?>, EntityType<HomareEntity>> SHIN_HOMARE = ENTITY_TYPES.register("shin_homare", () -> EntityType.Builder.of(HomareEntity::new, MobCategory.CREATURE).sized(0.6F,1.8F).build("kencraft:shin_homare"));
    public static final DeferredHolder<EntityType<?>, EntityType<HomareEntity>> KAORI_HOMARE = ENTITY_TYPES.register("kaori_homare", () -> EntityType.Builder.of(HomareEntity::new, MobCategory.CREATURE).sized(0.6F,1.8F).build("kencraft:kaori_homare"));
    /** Training-only entity; MISC prevents it from participating in natural mob spawning. */
    public static final DeferredHolder<EntityType<?>, EntityType<InteriorSpiritEntity>> INTERIOR_SPIRIT = ENTITY_TYPES.register("interior_spirit", () -> EntityType.Builder.of(InteriorSpiritEntity::new, MobCategory.MISC).sized(0.6F,1.8F).build("kencraft:interior_spirit"));

    private KenCraftEntities() {}

    public static final class HomareEntity extends PathfinderMob {
        public HomareEntity(EntityType<? extends PathfinderMob> type, net.minecraft.world.level.Level level) {
            super(type, level);
            this.setPersistenceRequired();
        }

        public static AttributeSupplier.Builder createAttributes() {
            return PathfinderMob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 20.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.20D)
                    .add(Attributes.FOLLOW_RANGE, 16.0D);
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(1, new FloatGoal(this));
            this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D));
            this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
            this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        }

        @Override
        protected InteractionResult mobInteract(Player player, InteractionHand hand) {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (level().isClientSide) return InteractionResult.SUCCESS;

            if (getType() == KAORI_HOMARE.get()) {
                StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
                if (progress.stage() >= 1) {
                    player.sendSystemMessage(Component.literal("Kaori: Você já falou comigo. Entre na cafeteria e fale com o Shin."));
                    return InteractionResult.CONSUME;
                }

                Race race = player.getData(ModAttachments.PLAYER_DATA).race();
                if (race == Race.RINKA) {
                    player.sendSystemMessage(Component.literal("Kaori: Ah, oi? Oque você faz aqui? Você tem um cheiro diferente... VOCÊ É UM RINKA! que legal, eu também sou uma Rinka, eu odeio investigadores ARF, creio que você também deve odiar eles, estou certa disso não é? Então entre, fale com o Shin, ele está sempre pronto para receber pessoas novas na cafeteria."));
                } else if (race == Race.HUMAN) {
                    player.sendSystemMessage(Component.literal("Kaori: Ah, oi? Oque você faz aqui? Você parece ser bem legal, eu adoro pessoas com o seu perfume, vamos entre e fale com o Shin."));
                } else if (race == Race.HYBRID) {
                    player.sendSystemMessage(Component.literal("Kaori: Ah, oi? Oque você faz aqui? Você tem um cheiro estranho... você não é exatamente Humano nem Rinka, né? Hahaha, tanto faz. Entre e fale com o Shin, ele vai querer saber quem você é."));
                } else if (race == Race.JASHIN) {
                    player.sendSystemMessage(Component.literal("Kaori: Ah, oi? Oque você faz aqui? Você tem um cheiro muito diferente... então você é um Jashin. Não tenho medo de você, mas entre e fale com o Shin."));
                } else {
                    player.sendSystemMessage(Component.literal("Kaori: Ah, oi? Oque você faz aqui? Escolha sua raça primeiro e depois volte a falar comigo."));
                    return InteractionResult.CONSUME;
                }

                player.setData(ModAttachments.STORY_PROGRESS, progress.withStage(1));
                player.sendSystemMessage(Component.literal("Missão da história: entre na cafeteria e fale com o Shin."));
                return InteractionResult.CONSUME;
            }

            if (getType() == SHIN_HOMARE.get()) {
                StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
                if (progress.stage() < 1) {
                    player.sendSystemMessage(Component.literal("Shin: Fale primeiro com a Kaori. Ela está esperando por você."));
                    return InteractionResult.CONSUME;
                }
                if (progress.stage() >= 2) {
                    player.sendSystemMessage(Component.literal("Shin: Você já concluiu esta parte da história. Continue sua jornada."));
                    return InteractionResult.CONSUME;
                }

                Race race = player.getData(ModAttachments.PLAYER_DATA).race();
                if (race == Race.RINKA) {
                    player.sendSystemMessage(Component.literal("Shin: Olá jogador(a) você deve ser o novo cliente, Você já conheceu a Kaori, então, oque você achou dela? Acho que você deve ter gostado dela, Senti seu cheiro de longe, você é Rinka, não precisa esconder, Você já conheceu o aodai? Vejo que sim, ele é bem orgulhoso, desculpa se ele te ofendeu, Os investigadores estão por toda parte, então recomendo você se preparar com uma Kikan."));
                } else if (race == Race.HUMAN) {
                    player.sendSystemMessage(Component.literal("Shin: Olá jogador(a) você deve ser o novo cliente, vejo que você já conheceu a Kaori, mas eu tenho uma coisa pra te dizer, SE VOCÊ FOR DA ARF E ME FAZER ALGUM MAL PRA MIM OU PRA MINHA FILHA... EU TE MATAREI!"));
                } else if (race == Race.HYBRID) {
                    player.sendSystemMessage(Component.literal("Shin: Então você é um Híbrido... interessante. Você já falou com a Kaori, então pode ficar. Só não arrume problemas dentro da cafeteria."));
                } else if (race == Race.JASHIN) {
                    player.sendSystemMessage(Component.literal("Shin: Você é um Jashin. Não gosto de surpresas, mas já falou com a Kaori. Entre, fique tranquilo e não faça nada contra minha família."));
                } else {
                    player.sendSystemMessage(Component.literal("Shin: Escolha sua raça antes de continuar a história."));
                    return InteractionResult.CONSUME;
                }

                player.setData(ModAttachments.STORY_PROGRESS, progress.withStage(2));
                player.sendSystemMessage(Component.literal("Ascensão 1 alcançada!"));
                player.sendSystemMessage(Component.literal("Limit Break liberado: seus atributos agora podem chegar a 40 pontos."));
                return InteractionResult.CONSUME;
            }

            return InteractionResult.PASS;
        }
    }
}
