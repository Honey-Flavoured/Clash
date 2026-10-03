package com.jsburg.clash;

import com.jsburg.clash.registry.AllEffects;
import com.jsburg.clash.registry.AllItems;
import com.jsburg.clash.registry.AllParticles;
import com.jsburg.clash.registry.AllSounds;
import com.jsburg.clash.registry.Config;
import com.jsburg.clash.registry.MiscRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Clash.MOD_ID)
public class Clash {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "clash";

    public static ResourceLocation rl(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public Clash(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);

        AllItems.ITEMS.register(modEventBus);
        AllItems.DATA_COMPONENTS.register(modEventBus);
        AllSounds.SOUNDS.register(modEventBus);
        AllParticles.PARTICLE_TYPES.register(modEventBus);
        AllEffects.EFFECTS.register(modEventBus);
        MiscRegistry.ENTITY_TYPES.register(modEventBus);
    }
}
