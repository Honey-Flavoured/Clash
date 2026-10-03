package com.jsburg.clash.registry;

import com.jsburg.clash.Clash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class AllEnchantments {
    public static final int FLURRY_MAX_LEVEL = 3;
    public static final float FLURRY_SPEED_PER_LEVEL = 0.2f;

    public static final ResourceKey<Enchantment> FLURRY = key("flurry");
    public static final ResourceKey<Enchantment> SWEET_SPOT = key("sweet_spot");
    public static final ResourceKey<Enchantment> AGILITY = key("agility");
    public static final ResourceKey<Enchantment> JAB = key("jab");

    public static final ResourceKey<Enchantment> BUTCHERY = key("butchery");
    public static final ResourceKey<Enchantment> RAMPAGE = key("rampage");
    public static final ResourceKey<Enchantment> RETALIATION = key("retaliation");

    public static final ResourceKey<Enchantment> SAILING = key("sailing");
    public static final ResourceKey<Enchantment> CRUSHING = key("crushing");
    public static final ResourceKey<Enchantment> EXECUTIONER = key("executioner");
    public static final ResourceKey<Enchantment> THRUM = key("thrum");
    public static final ResourceKey<Enchantment> WHIRLING = key("whirling");

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Clash.rl(name));
    }

    public static int level(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        int found = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet()) {
            if (entry.getKey().is(enchantment)) {
                found = entry.getIntValue();
            }
        }
        return found;
    }
}
