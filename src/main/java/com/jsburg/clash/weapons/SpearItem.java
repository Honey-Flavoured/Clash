package com.jsburg.clash.weapons;

import com.jsburg.clash.enchantments.spear.DashEnchantment;
import com.jsburg.clash.registry.AllEnchantments;
import com.jsburg.clash.registry.AllParticles;
import com.jsburg.clash.registry.AllSounds;
import com.jsburg.clash.util.TextHelper;
import com.jsburg.clash.weapons.util.AttackHelper;
import com.jsburg.clash.weapons.util.ISpearAnimation;
import com.jsburg.clash.weapons.util.WeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class SpearItem extends WeaponItem implements ISpearAnimation {
    private static final Vec3 UP = new Vec3(0, 1, 0);
    private static final float stabLengthBonus = 2.5f;
    private static final float sweetSpotSize = 2.5f;

    public SpearItem(float attackDamage, float attackSpeed, Item.Properties properties) {
        super(attackDamage, attackSpeed, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.clash.spear.when_charged").withStyle(ChatFormatting.GRAY));
        tooltip.add(TextHelper.getBonusText("item.clash.spear.charge_range_bonus", stabLengthBonus));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 720000;
    }

    public int getMaxCharge(ItemStack stack) {
        if (AllEnchantments.level(stack, AllEnchantments.JAB) > 0) return 3;
        return 20;
    }

    public int getMinCharge(ItemStack stack) {
        if (AllEnchantments.level(stack, AllEnchantments.JAB) > 0) return 0;
        return 10 - AllEnchantments.level(stack, AllEnchantments.FLURRY);
    }

    protected void onStabHit(ItemStack stack, Player player, LivingEntity target, float chargePercent) {
        Vec3 look = player.getLookAngle();
        target.knockback(chargePercent / 3, -look.x(), -look.z());
    }

    protected boolean canStabCrit(ItemStack stack) {
        return true;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;
        int chargeTime = getUseDuration(stack, player) - timeLeft;
        ItemStack spear = player.getUseItem();

        float chargePercent = Math.min((float) chargeTime / getMaxCharge(spear), 1);
        boolean hasJab = AllEnchantments.level(stack, AllEnchantments.JAB) > 0;

        if (chargeTime < getMinCharge(stack)) return;

        player.awardStat(Stats.ITEM_USED.get(this));
        player.swing(player.getUsedItemHand());

        if (hasJab) {
            ItemCooldowns tracker = player.getCooldowns();
            tracker.removeCooldown(this);
            tracker.addCooldown(this, 40);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        }

        double stabLength = AttackHelper.getAttackRange(player) + stabLengthBonus;
        Vec3 look = player.getLookAngle();
        Vec3 endVec = look.scale(stabLength);
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 endPos = eyePos.add(endVec);
        AABB boundingBox = new AABB(eyePos.x, eyePos.y, eyePos.z, endPos.x, endPos.y, endPos.z).inflate(1);
        Predicate<Entity> predicate = (e) -> !e.isSpectator() && e.isPickable();
        EntityHitResult rayTraceResult = AttackHelper.rayTraceWithMotion(worldIn, player, eyePos, endPos, boundingBox, predicate);

        Vec3 side = look.cross(UP).scale(0.75);
        if (player.getMainArm() == HumanoidArm.LEFT ^ player.getUsedItemHand() == InteractionHand.OFF_HAND) {
            side = side.scale(-1);
        }
        side = side.add(eyePos).subtract(UP.scale(.2));

        if (rayTraceResult != null) {
            Entity target = rayTraceResult.getEntity();
            Vec3 hitLocation = rayTraceResult.getLocation();

            BlockHitResult blockRayTraceResult = player.level().clip(new ClipContext(eyePos, hitLocation, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (blockRayTraceResult.getType() == HitResult.Type.MISS && !worldIn.isClientSide) {
                if (target instanceof EnderDragonPart part) {
                    target = part.parentMob;
                }
                boolean canAttack = AttackHelper.fullAttackEntityCheck(player, target);
                if (canAttack && target instanceof LivingEntity living) {
                    float damage = (float) AttackHelper.getAttackDamage(spear, player, EquipmentSlot.MAINHAND);
                    if (canStabCrit(stack) && chargeTime > getMaxCharge(stack) - 4) {
                        damage *= AttackHelper.getCrit(player, target, true);
                    }
                    player.resetAttackStrengthTicker();

                    if (AllEnchantments.level(spear, AllEnchantments.SWEET_SPOT) > 0) {
                        double distance = endPos.distanceTo(hitLocation);
                        if (distance <= sweetSpotSize) {
                            damage *= 2;
                            AttackHelper.playSound(player, AllSounds.WEAPON_SPEAR_MEGA_CRIT.get(), 2f, 1.0f);
                            for (int i = 0; i <= 5; i++) {
                                RandomSource rand = worldIn.getRandom();
                                Vec3 motion = new Vec3(rand.nextDouble() - .5, rand.nextDouble() - .5, rand.nextDouble() - .5);
                                AttackHelper.makeParticleServer((ServerLevel) worldIn, AllParticles.SPEAR_CRIT.get(), hitLocation, motion, 1.5f);
                            }
                        }
                    }
                    damage += AttackHelper.getBonusEnchantmentDamage(spear, target, player);

                    AttackHelper.attackEntity(player, target, damage);
                    AttackHelper.doHitStuff(player, target, spear);
                    AttackHelper.playSound(player, SoundEvents.PLAYER_ATTACK_STRONG);
                    this.onStabHit(stack, player, living, chargePercent);
                }
                player.causeFoodExhaustion(0.2f);
            }
        }

        AttackHelper.playSound(player, AllSounds.WEAPON_SPEAR_STAB.get());
        AttackHelper.makeParticle(player.getCommandSenderWorld(), AllParticles.SPEAR_STAB.get(), side.add(look), side.vectorTo(endPos), 1.4);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        DashEnchantment.tryAgilityDash(worldIn, playerIn, stack);
        playerIn.startUsingItem(handIn);
        return InteractionResultHolder.consume(stack);
    }
}
