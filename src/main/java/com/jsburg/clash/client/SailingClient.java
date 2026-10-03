package com.jsburg.clash.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public final class SailingClient {
    private SailingClient() {}

    public static float adjustSpeed(Player player, float speed) {
        if (player instanceof LocalPlayer client) {
            if (client.input.up) speed += 0.5f;
            if (client.input.down) speed -= 0.5f;
        }
        return speed;
    }
}
