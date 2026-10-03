package com.jsburg.clash.event;

import com.jsburg.clash.Clash;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Clash.MOD_ID)
public class CreativeModeTabEvents {
    private static final List<Supplier<? extends ItemLike>> ITEMS = new ArrayList<>();
    private static final List<ResourceKey<CreativeModeTab>> TABS = new ArrayList<>();

    @SafeVarargs
    public static void assignItemToTab(Supplier<? extends ItemLike> item, ResourceKey<CreativeModeTab>... tabs) {
        for (ResourceKey<CreativeModeTab> tab : tabs) {
            ITEMS.add(item);
            TABS.add(tab);
        }
    }

    @SubscribeEvent
    public static void onCreativeTabRegister(BuildCreativeModeTabContentsEvent event) {
        for (int i = 0; i < ITEMS.size(); i++) {
            if (event.getTabKey() == TABS.get(i)) {
                event.accept(ITEMS.get(i).get());
            }
        }
    }
}
