package com.jsburg.clash.weapons;

import com.jsburg.clash.registry.AllEffects;
import com.jsburg.clash.registry.AllParticles;
import com.jsburg.clash.util.ScreenShaker;
import com.jsburg.clash.weapons.util.AttackHelper;
import com.jsburg.clash.weapons.util.WeaponItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SweptAxeItem extends WeaponItem {
    public SweptAxeItem(int attackDamage, float attackSpeed, Properties properties) {
        super(attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (AttackHelper.weaponIsCharged(player)) {
            MobEffectInstance retaliation = player.getEffect(AllEffects.RETALIATION);
            Vec3 eyepos = player.position().add(0, player.getEyeHeight(), 0);
            AttackHelper.makeParticle(target.level(), AllParticles.AXE_SWEEP.get(),
                    target.position().add(eyepos).scale(.5),
                    .5, retaliation != null ? 1 : 0, player.getMainArm() == HumanoidArm.LEFT ? 1 : 0
            );

            Vec3 look = player.getLookAngle();
            float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            int hits = 1;

            for (LivingEntity livingentity : player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(1.5D, 0.5D, 1.5D))) {
                if (livingentity != player && livingentity != target && !player.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStand stand) || !stand.isMarker()) && player.distanceToSqr(livingentity) < 12.0D) {
                    if (livingentity instanceof TamableAnimal pet && pet.isOwnedBy(player)) {
                        continue;
                    }
                    if (!player.level().isClientSide) {
                        livingentity.knockback(0.4f, -look.x(), -look.z());
                        float bonus = AttackHelper.getBonusEnchantmentDamage(stack, livingentity, player);
                        if (livingentity.hurt(player.level().damageSources().playerAttack(player), damage + bonus)) {
                            if (bonus > 0) {
                                player.magicCrit(livingentity);
                            }
                        }
                    }
                    hits++;
                }
            }

            if (player.level().isClientSide && hits >= 3) {
                ScreenShaker.setScreenShake(4, 2);
            }

            AttackHelper.playSound(player, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0f, .7f);
        }
        return false;
    }
}
