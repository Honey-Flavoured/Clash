package com.jsburg.clash.enchantments.axe;

import com.jsburg.clash.registry.AllEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class RampageEnchantment {
    private RampageEnchantment() {}

    public static void onKill(LivingEntity user, int level) {
        MobEffectInstance rampageStacks = user.getEffect(AllEffects.RAMPAGING);
        int amp = rampageStacks == null ? -1 : rampageStacks.getAmplifier();
        user.addEffect(new MobEffectInstance(AllEffects.RAMPAGING, 30 + 20 * level, amp + 1, false, false));
    }
}
