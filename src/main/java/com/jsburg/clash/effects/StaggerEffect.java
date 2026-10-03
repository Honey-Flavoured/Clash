package com.jsburg.clash.effects;

import com.jsburg.clash.util.MiscHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class StaggerEffect extends MobEffect {
    public StaggerEffect() {
        super(MobEffectCategory.HARMFUL, 16777215);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.onGround()) {
            Vec3 motion = entity.getDeltaMovement();
            Vec3 look = MiscHelper.extractHorizontal(entity.getViewVector(1)).scale(-1);
            float movespeed = entity.getSpeed();
            float speed = movespeed / (1 + amplifier);
            double dot = motion.dot(look);
            if (dot < speed) {
                entity.setDeltaMovement(motion.add(look.scale(speed - dot)));
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
