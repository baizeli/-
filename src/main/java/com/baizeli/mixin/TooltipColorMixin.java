package com.baizeli.mixin;

import net.minecraftforge.client.event.RenderTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTooltipEvent.Color.class)
public abstract class TooltipColorMixin {

    // 颜色定义
    @Unique
    private static final int  SMMOJANG = 0xFF4FC3F7;  // 浅蓝 (RGB: 79,195,247)
    @Unique
    private static final int SBMOJANG = 0xFFFFFFFF; // 纯白 (RGB: 255,255,255)

    // 渐变周期（毫秒）
    @Unique
    private static final long CNMSB = 3000; // 3秒完成一次蓝→白→蓝循环

    @Unique
    private int fuckYou() {
        float progress = (System.currentTimeMillis() % CNMSB) / (float) CNMSB;

        float lerpFactor = (float) Math.sin(progress * Math.PI * 2) * 0.5f + 0.5f;

        return sb(SMMOJANG, SBMOJANG, lerpFactor);
    }

    @Unique
    private int sb(int startColor, int endColor, float factor) {
        int C = (int) ((startColor >> 16 & 0xFF) * (1 - factor) + (endColor >> 16 & 0xFF) * factor);
        int N = (int) ((startColor >> 8 & 0xFF) * (1 - factor) + (endColor >> 8 & 0xFF) * factor);
        int M = (int) ((startColor & 0xFF) * (1 - factor) + (endColor & 0xFF) * factor);
        return 0xFF000000 | (C << 16) | (N << 8) | M; // 固定不透明度
    }

    @Inject(method = "getBorderStart", at = @At("HEAD"), cancellable = true, remap = false)
    private void CNMDESB(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(fuckYou());
    }

    @Inject(method = "getBorderEnd", at = @At("HEAD"), cancellable = true, remap = false)
    private void FUCK(CallbackInfoReturnable<Integer> cir) {
        // 边框结束色比开始色延迟1/4周期，形成动态渐变
        long offsetTime = (System.currentTimeMillis() + CNMSB / 4) % CNMSB;
        float progress = offsetTime / (float) CNMSB;
        float lerpFactor = (float) Math.sin(progress * Math.PI * 2) * 0.5f + 0.5f;
        cir.setReturnValue(sb(SMMOJANG, SBMOJANG, lerpFactor));
    }

    @Inject(method = "getBackgroundStart", at = @At("HEAD"), cancellable = true, remap = false)
    private void overrideBackgroundStart(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(fuckYou() & 0x77FFFFFF); // 47%透明度
    }

    @Inject(method = "getBackgroundEnd", at = @At("HEAD"), cancellable = true, remap = false)
    private void overrideBackgroundEnd(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(fuckYou() & 0x55FFFFFF); // 33%透明度
    }

}