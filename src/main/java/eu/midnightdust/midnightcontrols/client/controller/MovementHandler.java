/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of midnightcontrols.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package eu.midnightdust.midnightcontrols.client.controller;

import eu.midnightdust.midnightcontrols.client.ButtonState;
import eu.midnightdust.midnightcontrols.client.MidnightControlsClient;
import eu.midnightdust.midnightcontrols.client.MidnightControlsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.tutorial.MovementTutorialStepHandler;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.SoulSpeedEnchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.scanner.SimpleNbtScanner;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the movement handler.
 *
 * @author LambdAurora
 * @version 1.6.0
 * @since 1.4.0
 */
public final class MovementHandler implements PressAction {
    public static final MovementHandler HANDLER = new MovementHandler();
    private boolean shouldOverrideMovement = false;
    private boolean pressingForward = false;
    private boolean pressingBack = false;
    private boolean pressingLeft = false;
    private boolean pressingRight = false;
    private float slowdownFactor = 1.f;
    private float movementForward = 0.f;
    private float movementSideways = 0.f;

    private MovementHandler() {
    }

    /**
     * Applies movement input of this handler to the player's input.
     *
     * @param player The client player.
     */
    public void applyMovement(@NotNull ClientPlayerEntity player) {
        double movementR, movementTheta;

        if (!this.shouldOverrideMovement)
            return;
        player.input.pressingForward = this.pressingForward;
        player.input.pressingBack = this.pressingBack;
        player.input.pressingLeft = this.pressingLeft;
        player.input.pressingRight = this.pressingRight;

        // Do an implicit square here
        movementR = this.slowdownFactor * (Math.pow(this.movementSideways, 2) + Math.pow(this.movementForward, 2));
        movementR = MathHelper.clamp(movementR, 0.f, 1.f);
        movementTheta = Math.atan2(this.movementForward, this.movementSideways);
        player.input.movementForward = (float) (movementR * Math.sin(movementTheta));
        player.input.movementSideways = (float) (movementR * Math.cos(movementTheta));

        this.shouldOverrideMovement = false;
    }

    @Override
    public boolean press(@NotNull MinecraftClient client, @NotNull ButtonBinding button, float value, @NotNull ButtonState action) {
        if (client.currentScreen != null || client.player == null)
            return this.shouldOverrideMovement = false;

        int direction = 0;
        if (button == ButtonBinding.FORWARD || button == ButtonBinding.LEFT)
            direction = 1;
        else if (button == ButtonBinding.BACK || button == ButtonBinding.RIGHT)
            direction = -1;

        if (action.isUnpressed())
            direction = 0;

        this.shouldOverrideMovement = direction != 0;

        if (!MidnightControlsConfig.analogMovement) {
            value = 1.f;
        }

        this.slowdownFactor = client.player.shouldSlowDown() ? (MathHelper.clamp(
            0.3F + EnchantmentHelper.getSwiftSneakSpeedBoost(client.player),
            0.0F,
            1.0F
        )) : 1.f;

        if (button == ButtonBinding.FORWARD || button == ButtonBinding.BACK) {
            // Handle forward movement.
            this.pressingForward = direction > 0;
            this.pressingBack = direction < 0;
            this.movementForward = direction * value;
        } else {
            // Handle sideways movement.
            this.pressingLeft = direction > 0;
            this.pressingRight = direction < 0;
            this.movementSideways = direction * value;
        }

        return this.shouldOverrideMovement;
    }
}
