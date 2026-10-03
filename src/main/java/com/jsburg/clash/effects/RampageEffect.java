package com.jsburg.clash.effects;

import com.jsburg.clash.mixin.LivingEntityAccessor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class RampageEffect extends MobEffect {
    public RampageEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
    }

    @Override
    public boolean applyEffectTick(LivingEntity target, int amplifier) {
        // Attack speed only shortens the cooldown. Extra progress has to be added directly
        // or a swing started before the effect ends is thrown away.
        if (target instanceof Player) {
            LivingEntityAccessor access = (LivingEntityAccessor) target;
            access.clash$setAttackStrengthTicker(access.clash$getAttackStrengthTicker() + amplifier + 1);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 2 == 0;
    }
}
