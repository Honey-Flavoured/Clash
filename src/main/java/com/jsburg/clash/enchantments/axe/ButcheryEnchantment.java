package com.jsburg.clash.enchantments.axe;

import com.jsburg.clash.registry.AllParticles;
import com.jsburg.clash.registry.MiscRegistry;
import com.jsburg.clash.weapons.util.AttackHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ButcheryEnchantment {
    private ButcheryEnchantment() {}

    public static float getDamageMultiplier(int level) {
        return 1 + (.15f * level);
    }

    public static int getPorkAmount(int level, RandomSource random) {
        int count = 0;
        for (int n = 1; n <= level; n++) {
            if (random.nextFloat() <= .5) {
                count++;
            }
        }
        return count;
    }

    public static boolean affectsEntity(LivingEntity target) {
        return target.getType().is(MiscRegistry.PORKY);
    }

    public static void onHit(int level, LivingEntity target) {
        if (!target.level().isClientSide) {
            RandomSource rand = target.level().getRandom();
            AABB bb = target.getBoundingBox();
            double l = bb.getSize();
            Vec3 pos = new Vec3((rand.nextDouble() - .5) * l, rand.nextDouble() * l + .5, (rand.nextDouble() - .5) * l);
            AttackHelper.makeParticleServer((ServerLevel) target.level(), AllParticles.BUTCHER_SPARK_EMITTER.get(), target.position().add(pos), Vec3.ZERO, 0);
        }
    }
}
