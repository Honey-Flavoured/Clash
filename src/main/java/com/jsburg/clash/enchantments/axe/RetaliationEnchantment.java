package com.jsburg.clash.enchantments.axe;

import com.jsburg.clash.registry.AllEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class RetaliationEnchantment {
    private RetaliationEnchantment() {}

    public static void onUserHurt(LivingEntity user, int level) {
        MobEffectInstance retaliation = user.getEffect(AllEffects.RETALIATION);
        int amp = retaliation == null ? -1 : retaliation.getAmplifier();
        user.addEffect(new MobEffectInstance(AllEffects.RETALIATION, 20 * 20, Mth.clamp(amp + 1, 0, level), false, true));
    }
}
