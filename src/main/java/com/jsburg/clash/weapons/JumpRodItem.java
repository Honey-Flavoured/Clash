package com.jsburg.clash.weapons;

import com.jsburg.clash.enchantments.spear.DashEnchantment;
import com.jsburg.clash.registry.AllSounds;
import com.jsburg.clash.weapons.util.AttackHelper;
import com.jsburg.clash.weapons.util.ISpearAnimation;
import com.jsburg.clash.weapons.util.WeaponItem;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class JumpRodItem extends WeaponItem implements ISpearAnimation {
    private static final int maxCharge = 18;
    private static final int minCharge = 5;

    public JumpRodItem(float attackDamage, float attackSpeed, Properties properties) {
        super(attackDamage, attackSpeed, properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 720000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;
        int chargeTime = getUseDuration(stack, player) - timeLeft;
        ItemStack rod = player.getUseItem();

        float chargePercent = Math.min((float) chargeTime / maxCharge, 1);
        boolean doThrust = !player.isShiftKeyDown() && chargeTime > minCharge && player.onGround() && !player.isSwimming();

        if (doThrust) {
            player.awardStat(Stats.ITEM_USED.get(this));
            player.swing(player.getUsedItemHand());
            AttackHelper.playSound(player, AllSounds.WEAPON_SPEAR_WHOOSH.get(), 0.3f, 1.0f);

            double boostedPercentage = Math.min(1, chargePercent * 1.4);
            Vec3 dir = player.getLookAngle().scale(2 * boostedPercentage);
            player.push(dir.x, dir.y / 2 + 0.2, dir.z);
            AttackHelper.damageItem(1, rod, player, player.getUsedItemHand());
            player.causeFoodExhaustion(0.1f);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        DashEnchantment.tryAgilityDash(worldIn, playerIn, stack);
        playerIn.startUsingItem(handIn);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getMaxCharge(ItemStack stack) {
        return maxCharge;
    }
}
