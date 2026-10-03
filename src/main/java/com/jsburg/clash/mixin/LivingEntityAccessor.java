package com.jsburg.clash.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("attackStrengthTicker")
    int clash$getAttackStrengthTicker();

    @Accessor("attackStrengthTicker")
    void clash$setAttackStrengthTicker(int value);
}
