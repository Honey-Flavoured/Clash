package com.jsburg.clash.mixin;

import com.jsburg.clash.weapons.util.IHitListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)Z"))
    private void clash$onAttack(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof IHitListener listener && target instanceof LivingEntity living) {
            listener.onHit(heldItem, living, player.getAttackStrengthScale(0.5F) > 0.9F);
        }
    }
}
