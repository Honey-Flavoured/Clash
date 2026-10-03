package com.jsburg.clash.registry;

import com.jsburg.clash.Clash;
import com.jsburg.clash.entity.GreatbladeSlashEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiscRegistry {
    public static final TagKey<EntityType<?>> PORKY = TagKey.create(Registries.ENTITY_TYPE, Clash.rl("porky_entities"));

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Clash.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<GreatbladeSlashEntity>> GREATBLADE_SLASH = registerEntityType("greatblade_slash",
            EntityType.Builder.of((EntityType<GreatbladeSlashEntity> type, Level level) -> new GreatbladeSlashEntity(type, level), MobCategory.MISC)
                    .sized(3, 3).clientTrackingRange(4).updateInterval(20).fireImmune().noSummon()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<GreatbladeSlashEntity>> GREATBLADE_SLASH_EXECUTIONER = registerEntityType("greatblade_slash_executioner",
            EntityType.Builder.of((EntityType<GreatbladeSlashEntity> type, Level level) -> new GreatbladeSlashEntity(type, level), MobCategory.MISC)
                    .sized(1, 3).clientTrackingRange(4).updateInterval(20).fireImmune().noSummon()
    );

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntityType(String id, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(id, () -> builder.build(Clash.MOD_ID + ":" + id));
    }
}
