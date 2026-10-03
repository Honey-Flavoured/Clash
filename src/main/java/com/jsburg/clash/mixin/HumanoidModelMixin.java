package com.jsburg.clash.mixin;

import com.google.common.collect.ImmutableSet;
import com.jsburg.clash.client.ThirdPersonPoses;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    private static final Set<InteractionHand> HANDS = ImmutableSet.of(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND);

    @Inject(method = "setupAttackAnimation", at = @At("HEAD"), cancellable = true)
    private <T extends LivingEntity> void onArmSwing(T entity, float ageInTicks, CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;

        for (InteractionHand hand : HANDS) {
            boolean active = player.isUsingItem() && hand == player.getUsedItemHand();
            ItemStack handItem = player.getItemInHand(hand);
            ThirdPersonPoses.AnimType type = ThirdPersonPoses.hasAnim(player, handItem, active, hand);
            if (type != ThirdPersonPoses.AnimType.FALSE) {
                boolean leftHanded = hand == InteractionHand.OFF_HAND ^ player.getMainArm() == HumanoidArm.LEFT;
                float partialTicks = ageInTicks % 1;
                ThirdPersonPoses.pose(player, (HumanoidModel<T>) (Object) this, handItem, partialTicks, leftHanded, active, hand);
                if (type == ThirdPersonPoses.AnimType.OVERWRITES) {
                    ci.cancel();
                }
                break;
            }
        }
    }
}
