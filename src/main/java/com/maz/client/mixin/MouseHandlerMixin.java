package com.maz.client.mixin;

import com.maz.client.module.CpsModule;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"))
    private void maz$recordMouseClick(
            long window,
            MouseButtonInfo input,
            int action,
            CallbackInfo ci
    ) {
        if (input.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && action == GLFW.GLFW_PRESS) {
            CpsModule.recordLeftClick();
        }
    }
}
