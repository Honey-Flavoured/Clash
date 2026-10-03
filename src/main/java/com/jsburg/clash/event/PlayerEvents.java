package com.jsburg.clash.event;

import com.jsburg.clash.Clash;
import com.jsburg.clash.registry.AllEffects;
import com.jsburg.clash.registry.AllParticles;
import com.jsburg.clash.weapons.util.AttackHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@EventBusSubscriber(modid = Clash.MOD_ID)
public class PlayerEvents {
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        MobEffectInstance retaliation = player.getEffect(AllEffects.RETALIATION);
        if (retaliation == null) return;

        Vec3 eyepos = player.position().add(0, player.getEyeHeight(), 0);
        AttackHelper.makeParticle(event.getTarget().level(), AllParticles.SCREEN_SHAKER.get(),
                event.getTarget().position().add(eyepos).scale(.5),
                (1 + retaliation.getAmplifier()) * .5, 2 + retaliation.getAmplifier() * 2, 12
        );
        player.removeEffect(AllEffects.RETALIATION);
    }
}
