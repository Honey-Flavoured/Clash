package com.jsburg.clash.effects;

import com.jsburg.clash.Clash;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class RetaliationEffect extends MobEffect {
    public RetaliationEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, Clash.rl("effect.retaliation"), 2.0, AttributeModifier.Operation.ADD_VALUE);
    }
}
