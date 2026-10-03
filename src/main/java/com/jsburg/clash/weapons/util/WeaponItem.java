package com.jsburg.clash.weapons.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WeaponItem extends Item {
    public WeaponItem(float attackDamage, float attackSpeed, Item.Properties properties) {
        super(withAttributes(properties, attackDamage, attackSpeed));
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    private static Properties withAttributes(Properties properties, float attackDamage, float attackSpeed) {
        // Constructor values match the tooltip. Attributes are stored relative to the player's base.
        float damage = attackDamage - 1;
        float speed = -(4 - attackSpeed);
        return properties.attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level worldIn, BlockPos pos, Player player) {
        return !player.isCreative();
    }
}
