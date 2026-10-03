package com.jsburg.clash.client;

import com.jsburg.clash.Clash;
import com.jsburg.clash.registry.AllItems;
import com.jsburg.clash.registry.AllParticles;
import com.jsburg.clash.registry.MiscRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Clash.MOD_ID, value = Dist.CLIENT)
public class ClashClient {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            AllItems.registerItemProperties();
            EntityRenderers.register(MiscRegistry.GREATBLADE_SLASH.get(), NoopRenderer::new);
            EntityRenderers.register(MiscRegistry.GREATBLADE_SLASH_EXECUTIONER.get(), NoopRenderer::new);
        });
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        AllParticles.registerParticleFactories(event);
    }
}
