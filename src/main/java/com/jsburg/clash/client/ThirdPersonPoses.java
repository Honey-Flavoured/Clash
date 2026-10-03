package com.jsburg.clash.client;

import com.jsburg.clash.util.ItemAnimator;
import com.jsburg.clash.util.MiscHelper;
import com.jsburg.clash.weapons.GreatbladeItem;
import com.jsburg.clash.weapons.JumpRodItem;
import com.jsburg.clash.weapons.SpearItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static com.jsburg.clash.util.MiscHelper.rotateX;

public final class ThirdPersonPoses {
    private ThirdPersonPoses() {}

    public enum AnimType {
        OVERWRITES,
        TRUE,
        FALSE;

        public static AnimType ifTrue(boolean test) {
            return test ? TRUE : FALSE;
        }
    }

    public static AnimType hasAnim(Player player, ItemStack stack, boolean active, InteractionHand hand) {
        Item item = stack.getItem();
        if (item instanceof SpearItem || item instanceof JumpRodItem) {
            return AnimType.ifTrue(active);
        }
        if (item instanceof GreatbladeItem) {
            if (active) return AnimType.OVERWRITES;
            if (ItemAnimator.getAnimation(GreatbladeItem.GreatbladeThirdPersonAnimation.class, player, hand) != null) {
                return AnimType.OVERWRITES;
            }
        }
        return AnimType.FALSE;
    }

    public static <T extends LivingEntity> void pose(Player player, HumanoidModel<T> model, ItemStack itemStack, float partialTicks, boolean leftHanded, boolean isActive, InteractionHand hand) {
        Item item = itemStack.getItem();
        if (item instanceof SpearItem) {
            poseSpear(model, leftHanded);
        } else if (item instanceof JumpRodItem) {
            poseRod(model, leftHanded);
        } else if (item instanceof GreatbladeItem sword) {
            poseGreatblade(player, model, itemStack, partialTicks, leftHanded, hand, sword);
        }
    }

    private static <T extends LivingEntity> void poseSpear(HumanoidModel<T> model, boolean leftHanded) {
        ModelPart spearArm = leftHanded ? model.leftArm : model.rightArm;
        ModelPart otherArm = leftHanded ? model.rightArm : model.leftArm;
        int sideFlip = leftHanded ? -1 : 1;

        spearArm.xRot *= .4f;
        spearArm.xRot -= .4f;
        spearArm.yRot -= .3f * sideFlip;
        spearArm.zRot += .3f * sideFlip;
        spearArm.x += 1f * sideFlip;
        spearArm.y += 4f;
        spearArm.z += 2f;

        otherArm.xRot *= .4f;
        otherArm.xRot -= .4f;
        otherArm.zRot += .9f * sideFlip;
        otherArm.x -= 2f * sideFlip;
        otherArm.y -= 2f;
        otherArm.z -= 3f;

        model.body.yRot += .3f * sideFlip;
    }

    private static <T extends LivingEntity> void poseRod(HumanoidModel<T> model, boolean leftHanded) {
        ModelPart spearArm = leftHanded ? model.leftArm : model.rightArm;
        int sideFlip = leftHanded ? -1 : 1;

        spearArm.xRot *= .4f;
        spearArm.xRot += .4f;
        spearArm.yRot += .3f * sideFlip;
        spearArm.zRot += .3f * sideFlip;
        spearArm.x += 1f * sideFlip;
        spearArm.y += 4f;
        spearArm.z += 2f;

        model.body.yRot += .3f * sideFlip;
    }

    private static <T extends LivingEntity> void poseGreatblade(Player player, HumanoidModel<T> model, ItemStack itemStack, float partialTicks, boolean leftHanded, InteractionHand hand, GreatbladeItem sword) {
        ModelPart swordArm = leftHanded ? model.leftArm : model.rightArm;
        ModelPart otherArm = leftHanded ? model.rightArm : model.leftArm;
        int sideFlip = leftHanded ? -1 : 1;
        ItemAnimator.ItemAnimation animation = ItemAnimator.getAnimation(GreatbladeItem.GreatbladeThirdPersonAnimation.class, player, hand);
        float swingProgress = animation == null ? 0 : animation.getProgress(partialTicks);

        int useCount = player.getUseItemRemainingTicks();
        int useDuration = sword.getUseDuration(itemStack, player);
        int useTime = useDuration - useCount;
        float chargePercent = Math.min((useTime + partialTicks) / sword.getMaxCharge(), 1);
        float chargeLerp = Math.min(chargePercent, 1 - swingProgress);

        swordArm.xRot *= .4f * chargeLerp;
        swordArm.xRot -= .4f * chargeLerp;
        swordArm.yRot -= .3f * sideFlip * chargeLerp;
        swordArm.zRot += .3f * sideFlip * chargeLerp;
        swordArm.x += 1f * sideFlip * chargeLerp;
        swordArm.y += 4f * chargeLerp;
        swordArm.z += 2f * chargeLerp;

        otherArm.xRot *= .4f * chargeLerp;
        otherArm.xRot -= .4f * chargeLerp;
        otherArm.zRot += .9f * sideFlip * chargeLerp;
        otherArm.x -= 2f * sideFlip * chargeLerp;
        otherArm.y -= 2f * chargeLerp;
        otherArm.z -= 3f * chargeLerp;

        model.body.yRot += .3f * sideFlip * chargeLerp;

        if (GreatbladeItem.hasSailing(itemStack) && useTime < 10) {
            model.leftLeg.xRot = 0;
            model.rightLeg.xRot = .3f;
        }

        if (animation != null) {
            float pi = (float) Math.PI;
            float swingPercent = (float) Math.sin(Math.pow(animation.getProgress(partialTicks), .6) * pi);

            swordArm.xRot -= .5 * pi * swingPercent;
            swordArm.zRot += .4 * pi * swingPercent * sideFlip;
            swordArm.yRot += .1 * pi * swingPercent * sideFlip;
            swordArm.x += 2.5f * sideFlip * swingPercent;
            swordArm.y -= 6f * swingPercent;
            swordArm.z -= 3f * swingPercent;

            otherArm.xRot -= .4 * pi * swingPercent;
            otherArm.zRot += .4 * pi * swingPercent * sideFlip;
            otherArm.yRot += .1 * pi * swingPercent;
            otherArm.x += 1f * sideFlip * swingPercent;
            otherArm.y += 3f * swingPercent;
            otherArm.z += 4.5f * swingPercent;

            model.body.yRot -= .2f * pi * swingPercent * sideFlip;
        }
    }

    public static <T extends LivingEntity, M extends EntityModel<T> & ArmedModel> boolean onRender(M model, LivingEntity entity, ItemStack itemStack, ItemDisplayContext context, HumanoidArm side, PoseStack poseStack, MultiBufferSource renderBuffer, int light) {
        if (!(itemStack.getItem() instanceof GreatbladeItem) || !(entity instanceof Player player)) return false;
        InteractionHand swordHand = MiscHelper.getHandFromSide(player, side);
        ItemAnimator.ItemAnimation animation = ItemAnimator.getAnimation(GreatbladeItem.GreatbladeThirdPersonAnimation.class, player, swordHand);
        if (animation != null) {
            float progress = animation.getProgress(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
            float pi = (float) Math.PI;
            float sineProgress = (float) Math.sin(progress * pi);
            rotateX(poseStack, sineProgress * 180);
            poseStack.translate(0, sineProgress * .25, sineProgress * -.25);
        }
        return false;
    }
}
