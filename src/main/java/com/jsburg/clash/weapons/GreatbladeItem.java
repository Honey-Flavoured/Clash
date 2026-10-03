package com.jsburg.clash.weapons;

import com.jsburg.clash.client.SailingClient;
import com.jsburg.clash.entity.GreatbladeSlashEntity;
import com.jsburg.clash.registry.AllEffects;
import com.jsburg.clash.registry.AllEnchantments;
import com.jsburg.clash.registry.AllItems;
import com.jsburg.clash.registry.AllParticles;
import com.jsburg.clash.util.ItemAnimator;
import com.jsburg.clash.util.MiscHelper;
import com.jsburg.clash.util.TextHelper;
import com.jsburg.clash.weapons.util.AttackHelper;
import com.jsburg.clash.weapons.util.IActiveResetListener;
import com.jsburg.clash.weapons.util.IHitListener;
import com.jsburg.clash.weapons.util.WeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GreatbladeItem extends WeaponItem implements IHitListener, IActiveResetListener {
    private final float baseDamage;

    public GreatbladeItem(float attackDamage, float attackSpeed, Properties properties) {
        super(attackDamage, attackSpeed, properties);
        baseDamage = attackDamage;
    }

    public float getSlashDamage(ItemStack stack) {
        float base = hasExecutioner(stack) ? 10 : 5;
        if (hasThrumEnchant(stack)) {
            base += 2 * thrumLevel(stack);
        }
        return base;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.clash.spear.when_charged").withStyle(ChatFormatting.GRAY));
        tooltip.add(TextHelper.getBonusText("item.clash.greatblade.bonus_damage", getSlashDamage(stack)));
    }

    public static boolean hasSailing(ItemStack stack) {
        return AllEnchantments.level(stack, AllEnchantments.SAILING) > 0;
    }

    private static int crushingLevel(ItemStack stack) {
        return AllEnchantments.level(stack, AllEnchantments.CRUSHING);
    }

    public static boolean hasExecutioner(ItemStack stack) {
        return AllEnchantments.level(stack, AllEnchantments.EXECUTIONER) > 0;
    }

    private static int thrumLevel(ItemStack stack) {
        return AllEnchantments.level(stack, AllEnchantments.THRUM);
    }

    private static boolean hasThrumEnchant(ItemStack stack) {
        return thrumLevel(stack) > 0;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 720000;
    }

    public int getMaxCharge() {
        return 15;
    }

    public static int swingTimeMax() {
        return 7;
    }

    @Override
    public void onHit(ItemStack stack, LivingEntity target, boolean isCharged) {
        if (isCharged) {
            if (thrumLevel(stack) > 0) setThrum(stack, true);
            int crushing = crushingLevel(stack);
            target.addEffect(new MobEffectInstance(AllEffects.STAGGERED, (int) (25 * (1 + ((float) crushing / 2))), crushing, false, true));
        }
    }

    private static void setThrum(ItemStack stack, boolean value) {
        if (value) {
            stack.set(AllItems.HAS_THRUM.get(), true);
        } else {
            stack.remove(AllItems.HAS_THRUM.get());
        }
    }

    public static boolean hasThrum(ItemStack stack) {
        return stack.getOrDefault(AllItems.HAS_THRUM.get(), false);
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        super.onUseTick(level, player, stack, count);
        if (hasSailing(stack)) {
            float n = getUseDuration(stack, player) - count;
            float speed = 1;
            if (level.isClientSide()) {
                speed = SailingClient.adjustSpeed((Player) player, speed);
            }
            Vec3 accel = MiscHelper.extractHorizontal(player.getViewVector(1)).scale(speed / ((n + 2) / 3));
            if (n < 10) {
                player.setDeltaMovement(accel.x, player.getDeltaMovement().y, accel.z);
                if (n % 2 == 0) {
                    AttackHelper.makeParticleServer(level, AllParticles.SAILING_TRAIL, player.position().add(0, 1, 0));
                }
            }
            if (n > 15) {
                player.releaseUsingItem();
            }
        } else {
            float n = getUseDuration(stack, player) - count;
            float threshold = 5;
            float diff = (getMaxCharge() - n);
            if (diff > 0 && diff <= threshold) {
                Vec3 accel = player.getViewVector(1);
                player.setDeltaMovement(player.getDeltaMovement().add(accel.scale((1 / threshold) / 2)));
            }
            if (n > getMaxCharge()) {
                player.releaseUsingItem();
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;
        int chargeTime = getUseDuration(stack, player) - timeLeft;
        ItemStack sword = player.getUseItem();

        if (chargeTime < getMaxCharge()) return;

        GreatbladeSlashEntity slash = new GreatbladeSlashEntity(worldIn, sword, player.position(), player, hasExecutioner(stack));
        slash.setDamage(baseDamage + getSlashDamage(stack));
        if (hasThrum(stack)) {
            slash.applyThrum();
            setThrum(stack, false);
        }
        slash.spriteFlip = (player.getMainArm() == HumanoidArm.LEFT ^ player.getUsedItemHand() == InteractionHand.OFF_HAND) ? 1 : 0;
        slash.setDeltaMovement(player.getViewVector(1).scale(2));
        slash.applyWhirlwind(AllEnchantments.level(stack, AllEnchantments.WHIRLING));
        worldIn.addFreshEntity(slash);

        AttackHelper.playSound(player, SoundEvents.PLAYER_ATTACK_SWEEP, 1, .5f);

        if (worldIn.isClientSide) {
            ItemAnimator.startAnimation(player, sword, player.getUsedItemHand(), new GreatbladeAnimation());
            ItemAnimator.startAnimation(player, sword, player.getUsedItemHand(), new GreatbladeThirdPersonAnimation());
        }

        Vec3 look = player.getViewVector(1);
        player.push(look.x / 2, 0, look.z / 2);

        player.resetAttackStrengthTicker();
        player.getCooldowns().addCooldown(stack.getItem(), 20);
        InteractionHand otherHand = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        if (!otherStack.isEmpty()) player.getCooldowns().addCooldown(otherStack.getItem(), 10);
        player.causeFoodExhaustion(.5f);
    }

    public void onSlashHit(ItemStack stack, LivingEntity target, Entity user) {
        AttackHelper.playSound(user, SoundEvents.PLAYER_ATTACK_STRONG);
    }

    @Override
    public void onHandReset(ItemStack stack, LivingEntity user) {
        if (user instanceof Player player && user.getTicksUsingItem() < getMaxCharge() && hasSailing(stack)) {
            player.setDeltaMovement(player.getDeltaMovement().scale(.5));
            player.resetAttackStrengthTicker();
            player.getCooldowns().addCooldown(stack.getItem(), 30);
            player.causeFoodExhaustion(.5f);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        playerIn.startUsingItem(handIn);
        return InteractionResultHolder.consume(stack);
    }

    public static class GreatbladeAnimation extends ItemAnimator.SimpleItemAnimation {
        public GreatbladeAnimation() {
            super(swingTimeMax());
        }
    }

    public static class GreatbladeThirdPersonAnimation extends ItemAnimator.SimpleItemAnimation {
        public GreatbladeThirdPersonAnimation() {
            super(swingTimeMax() + 4);
        }
    }
}
