package com.jsburg.clash.registry;

import com.jsburg.clash.Clash;
import com.jsburg.clash.event.CreativeModeTabEvents;
import com.jsburg.clash.weapons.BillhookItem;
import com.jsburg.clash.weapons.GreatbladeItem;
import com.jsburg.clash.weapons.JumpRodItem;
import com.jsburg.clash.weapons.SpearItem;
import com.jsburg.clash.weapons.SweptAxeItem;
import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class AllItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Clash.MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Clash.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> HAS_THRUM = DATA_COMPONENTS.registerComponentType(
            "has_thrum",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DeferredItem<Item> SPEAR = register("spear", () -> new SpearItem(5, 2.0F, new Item.Properties().durability(852)));
    public static final DeferredItem<Item> BILLHOOK = register("billhook", () -> new BillhookItem(5, 2.0F, new Item.Properties().durability(852)));
    public static final DeferredItem<Item> SWEPT_AXE = register("swept_axe", () -> new SweptAxeItem(6, 0.9F, new Item.Properties().durability(650)));
    public static final DeferredItem<Item> SWEPT_AXE_HEAD = register("swept_axe_head", () -> new Item(new Item.Properties().stacksTo(1)), CreativeModeTabs.INGREDIENTS);
    public static final DeferredItem<Item> GREATBLADE = register("greatblade", () -> new GreatbladeItem(5, 0.8F, new Item.Properties().durability(896)));
    public static final DeferredItem<Item> ROD_OF_GALES = register("rod_of_gales", () -> new JumpRodItem(2, 2.0F, new Item.Properties().durability(612)));

    public static void registerItemProperties() {
        ItemProperties.register(GREATBLADE.get(), Clash.rl("has_thrum"),
                (itemStack, world, entity, seed) -> GreatbladeItem.hasThrum(itemStack) ? 1 : 0);
    }

    private static <T extends Item> DeferredItem<T> register(String name, Supplier<T> supplier) {
        return register(name, supplier, CreativeModeTabs.COMBAT);
    }

    private static <T extends Item> DeferredItem<T> register(String name, Supplier<T> supplier, ResourceKey<CreativeModeTab> tab) {
        var item = ITEMS.register(name, supplier);
        CreativeModeTabEvents.assignItemToTab(item, tab);
        return item;
    }
}
