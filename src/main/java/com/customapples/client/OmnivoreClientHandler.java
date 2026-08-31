package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.customapples.event.OmnivoreHandler;
import com.customapples.item.BreadAppleItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OmnivoreClientHandler {
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldShowOmnivoreEat(player)) {
            return;
        }
        if (event.getHand() != player.getUsedItemHand()) {
            return;
        }
        HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        applyEatTransform(event.getPoseStack(), event.getPartialTick(), arm, player, player.getUseItem());
    }

    /** Replicates vanilla ItemInHandRenderer eat animation for non-food omnivore items. */
    private static void applyEatTransform(
            PoseStack poseStack, float partialTick, HumanoidArm arm, Player player, ItemStack stack) {
        int useDuration = stack.getUseDuration();
        if (useDuration <= 0) {
            useDuration = OmnivoreHandler.EAT_DURATION;
        }
        float useTime = (float) player.getUseItemRemainingTicks() - partialTick + 1.0F;
        float progress = useTime / (float) useDuration;
        if (progress < 0.8F) {
            float bob = Mth.abs(Mth.cos(useTime / 4.0F * (float) Math.PI) * 0.1F);
            poseStack.translate(0.0F, bob, 0.0F);
        }
        float transform = 1.0F - (float) Math.pow(progress, 27.0D);
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(transform * 0.6F * direction, transform * -0.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(direction * transform * 90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(transform * 10.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(direction * transform * 30.0F));
    }

    private static boolean shouldShowOmnivoreEat(Player player) {
        if (!player.isUsingItem() || !BreadAppleItem.isOmnivoreActive(player)) {
            return false;
        }
        ItemStack stack = player.getUseItem();
        return OmnivoreHandler.isOmnivoreCandidate(stack);
    }
}
