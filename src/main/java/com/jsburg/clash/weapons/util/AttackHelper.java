package com.jsburg.clash.weapons.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AttackHelper {
    public static boolean canAttackEntity(Player player, Entity targetEntity) {
        return targetEntity.isAttackable() && !targetEntity.skipAttackInteraction(player);
    }

    public static boolean fullAttackEntityCheck(Player player, Entity targetEntity) {
        if (!CommonHooks.onPlayerAttackTarget(player, targetEntity)) return false;
        return canAttackEntity(player, targetEntity);
    }

    public static boolean weaponIsCharged(Player player) {
        return player.getAttackStrengthScale(0.5F) > 0.9F;
    }

    public static void playSound(Entity player, SoundEvent sound) {
        playSound(player, sound, 1.0F, 1.0F);
    }

    public static void playSound(Entity player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, player.getSoundSource(), volume, pitch);
    }

    public static void doHitStuff(Player player, Entity target, ItemStack weapon) {
        doHitStuff(player, target, weapon, player.getUsedItemHand());
    }

    public static void doHitStuff(Player player, Entity target, ItemStack weapon, InteractionHand hand) {
        if (target instanceof EnderDragonPart part) {
            target = part.parentMob;
        }
        if (!player.level().isClientSide && target instanceof LivingEntity living) {
            EquipmentSlot slot = hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
            weapon.hurtAndBreak(1, player, slot);
            if (!weapon.isEmpty() && weapon.getItem() instanceof IHitListener listener) {
                listener.onHit(weapon, living, true);
            }
        }
    }

    public static void damageItem(int damage, ItemStack item, Player player) {
        damageItem(damage, item, player, InteractionHand.MAIN_HAND);
    }

    public static void damageItem(int damage, ItemStack item, Player player, InteractionHand hand) {
        EquipmentSlot slot = hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
        item.hurtAndBreak(damage, player, slot);
    }

    public static double getAttackRange(Player player) {
        return player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
    }

    public static void attackEntity(Player player, Entity target, float damage) {
        float lastHealth = 0;
        if (target instanceof LivingEntity living) {
            lastHealth = living.getHealth();
        }
        target.hurt(player.level().damageSources().playerAttack(player), damage);

        if (target instanceof LivingEntity living) {
            float healthDifference = lastHealth - living.getHealth();
            player.awardStat(Stats.DAMAGE_DEALT, Math.round(healthDifference * 10));

            if (player.level() instanceof ServerLevel server && healthDifference > 2.0F) {
                int k = (int) (healthDifference * 0.5D);
                server.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), k, 0.1, 0.0, 0.1, 0.2);
            }
        }
    }

    public static float getBonusEnchantmentDamage(ItemStack item, Entity target, @Nullable Entity attacker) {
        if (!(target.level() instanceof ServerLevel server) || item.isEmpty()) return 0;
        DamageSource source = attacker instanceof Player player
                ? server.damageSources().playerAttack(player)
                : attacker instanceof LivingEntity living
                ? server.damageSources().mobAttack(living)
                : server.damageSources().generic();
        return EnchantmentHelper.modifyDamage(server, item, target, source, 0);
    }

    public static float getCrit(Player player, Entity target, boolean shouldCrit) {
        return getCrit(player, target, shouldCrit, true, true);
    }

    public static float getCrit(Player player, Entity target, boolean shouldCrit, boolean doEffects, boolean playCritSound) {
        CriticalHitEvent hitResult = CommonHooks.fireCriticalHit(player, target, shouldCrit, shouldCrit ? 1.5F : 1.0F);
        if (!hitResult.isCriticalHit()) {
            return 1;
        }
        if (hitResult.getDamageMultiplier() > 1 && doEffects) {
            if (playCritSound) playSound(player, SoundEvents.PLAYER_ATTACK_CRIT);
            player.crit(target);
        }
        return hitResult.getDamageMultiplier();
    }

    public static void makeParticle(Level world, SimpleParticleType particle, Vec3 position, Vec3 motion, double speed) {
        motion = motion.normalize().scale(speed);
        world.addParticle(particle, position.x(), position.y(), position.z(), motion.x(), motion.y(), motion.z());
    }

    public static void makeParticle(Level world, SimpleParticleType particle, Vec3 position, double xSpeed, double ySpeed, double zSpeed) {
        world.addParticle(particle, position.x(), position.y(), position.z(), xSpeed, ySpeed, zSpeed);
    }

    public static void makeParticle(Level world, SimpleParticleType particle, Vec3 position) {
        makeParticle(world, particle, position, Vec3.ZERO, 0);
    }

    public static void makeParticleServer(ServerLevel world, SimpleParticleType particle, Vec3 position, Vec3 motion, double speed) {
        motion = motion.normalize().scale(speed);
        world.sendParticles(particle, position.x(), position.y(), position.z(), 0, motion.x(), motion.y(), motion.z(), speed);
    }

    public static void makeParticleServer(Level world, Supplier<SimpleParticleType> particle, Vec3 position) {
        makeParticleServer(world, particle, position, 0, 0, 0);
    }

    public static void makeParticleServer(Level world, Supplier<SimpleParticleType> particle, Vec3 position, double xSpeed, double ySpeed, double zSpeed) {
        if (world.isClientSide()) return;
        ((ServerLevel) world).sendParticles(particle.get(), position.x(), position.y(), position.z(), 0, xSpeed, ySpeed, zSpeed, 1);
    }

    public static double getAttackDamage(ItemStack item, Player player, EquipmentSlot equipmentSlot) {
        double damage = player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        for (ItemAttributeModifiers.Entry entry : item.getAttributeModifiers().modifiers()) {
            if (entry.slot().test(equipmentSlot) && entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                damage += entry.modifier().amount();
            }
        }
        return damage;
    }

    @Nullable
    public static EntityHitResult rayTraceWithMotion(Level worldIn, Entity projectile, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter) {
        double d0 = Double.MAX_VALUE;
        Entity entity = null;
        Vec3 hitVec = null;

        for (Entity target : worldIn.getEntities(projectile, boundingBox, filter)) {
            Vec3 targetMotion = target.getDeltaMovement().scale(.5f);
            AABB entityBox = target.getBoundingBox().expandTowards(targetMotion).expandTowards(targetMotion.reverse());
            Optional<Vec3> optional = entityBox.clip(startVec, endVec);

            if (optional.isPresent()) {
                double d1 = startVec.distanceToSqr(optional.get());
                if (d1 < d0) {
                    hitVec = optional.get();
                    entity = target;
                    d0 = d1;
                }
            }
        }

        return entity == null ? null : new EntityHitResult(entity, hitVec);
    }

    public static Vec3 getEntityPosition(Entity target) {
        return new Vec3(target.getX(), target.getY(), target.getZ());
    }
}
