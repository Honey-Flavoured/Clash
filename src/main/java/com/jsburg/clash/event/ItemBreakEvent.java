package com.jsburg.clash.event;

import com.jsburg.clash.Clash;
import com.jsburg.clash.registry.AllItems;
import com.jsburg.clash.registry.Config;
import com.jsburg.clash.weapons.SweptAxeItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;

@EventBusSubscriber(modid = Clash.MOD_ID)
public class ItemBreakEvent {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemBroken(PlayerDestroyItemEvent event) {
        ItemStack item = event.getOriginal();
        if (item.getItem() instanceof SweptAxeItem && Config.SWEPT_AXE_HEAD_DROP.get()) {
            ItemStack headItem = new ItemStack(AllItems.SWEPT_AXE_HEAD.get(), 1);
            ItemEnchantments enchantments = item.get(DataComponents.ENCHANTMENTS);
            if (enchantments != null) {
                headItem.set(DataComponents.ENCHANTMENTS, enchantments);
            }
            Player player = event.getEntity();
            player.getCommandSenderWorld().addFreshEntity(new ItemEntity(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), headItem));
        }
    }
}
