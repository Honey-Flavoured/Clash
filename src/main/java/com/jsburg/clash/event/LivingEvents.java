package com.jsburg.clash.event;

import com.jsburg.clash.Clash;
import com.jsburg.clash.enchantments.axe.ButcheryEnchantment;
import com.jsburg.clash.enchantments.axe.RampageEnchantment;
import com.jsburg.clash.enchantments.axe.RetaliationEnchantment;
import com.jsburg.clash.registry.AllEffects;
import com.jsburg.clash.registry.AllEnchantments;
import com.jsburg.clash.registry.AllItems;
import com.jsburg.clash.registry.AllParticles;
import com.jsburg.clash.weapons.util.AttackHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = Clash.MOD_ID)
public class LivingEvents {
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.isRecentlyHit() && wasMeleeCaused(event.getSource())) {
            LivingEntity source = (LivingEntity) event.getSource().getEntity();
            LivingEntity target = event.getEntity();

            ItemStack weapon = source.getMainHandItem();
            int butcherLevel = AllEnchantments.level(weapon, AllEnchantments.BUTCHERY);
            if (butcherLevel > 0 && ButcheryEnchantment.affectsEntity(target) && weapon.getItem() != AllItems.SWEPT_AXE_HEAD.get()) {
                RandomSource random = target.getCommandSenderWorld().getRandom();
                int porkCount = ButcheryEnchantment.getPorkAmount(butcherLevel, random);
                if (porkCount > 0) {
                    ItemEntity porkDrop = new ItemEntity(target.getCommandSenderWorld(),
                            target.getX() + random.nextDouble() - .5,
                            target.getY() + random.nextDouble() + .5,
                            target.getZ() + random.nextDouble() - .5,
                            new ItemStack(Items.PORKCHOP, porkCount));
                    if (!target.getCommandSenderWorld().isClientSide) {
                        Vec3 pos = AttackHelper.getEntityPosition(porkDrop);
                        AttackHelper.makeParticleServer((ServerLevel) target.getCommandSenderWorld(), AllParticles.BONUS_DROP.get(), pos, Vec3.ZERO, 0);
                    }
                    event.getDrops().add(porkDrop);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingIncomingDamageEvent event) {
        if (wasMeleeCaused(event.getSource())) {
            LivingEntity source = (LivingEntity) event.getSource().getEntity();
            LivingEntity target = event.getEntity();

            ItemStack weapon = source.getMainHandItem();
            int butcherLevel = AllEnchantments.level(weapon, AllEnchantments.BUTCHERY);
            if (butcherLevel > 0 && ButcheryEnchantment.affectsEntity(target) && weapon.getItem() != AllItems.SWEPT_AXE_HEAD.get()) {
                event.setAmount(event.getAmount() * ButcheryEnchantment.getDamageMultiplier(butcherLevel));
                ButcheryEnchantment.onHit(butcherLevel, target);
            }
        }
        if (event.getEntity() instanceof Player hurtPlayer) {
            ItemStack heldItem = hurtPlayer.getMainHandItem();
            int retaliation = AllEnchantments.level(heldItem, AllEnchantments.RETALIATION);
            if (retaliation > 0) {
                RetaliationEnchantment.onUserHurt(hurtPlayer, retaliation);
            }
        }
        if (event.getEntity().getEffect(AllEffects.STAGGERED) != null) {
            event.getEntity().removeEffect(AllEffects.STAGGERED);
        }
    }

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (wasMeleeCaused(event.getSource())) {
            LivingEntity source = (LivingEntity) event.getSource().getEntity();
            ItemStack weapon = source.getMainHandItem();
            int rampageLevel = AllEnchantments.level(weapon, AllEnchantments.RAMPAGE);
            if (rampageLevel > 0) {
                RampageEnchantment.onKill(source, rampageLevel);
            }
        }
    }

    private static boolean wasMeleeCaused(DamageSource source) {
        return source.getEntity() instanceof LivingEntity && source.getDirectEntity() == source.getEntity();
    }
}
