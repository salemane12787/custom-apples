package com.customapples.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealAppleItemRenderer extends BlockEntityWithoutLevelRenderer {
  private static RealAppleItemRenderer instance;

  public RealAppleItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
    super(dispatcher, modelSet);
  }

  public static RealAppleItemRenderer getInstance() {
    if (instance == null) {
      Minecraft mc = Minecraft.getInstance();
      instance = new RealAppleItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
    }
    return instance;
  }

  @Override
  public void renderByItem(
      ItemStack stack,
      ItemDisplayContext displayContext,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int combinedLight,
      int combinedOverlay) {
    poseStack.pushPose();
    RealAppleMesh mesh = RealAppleMesh.get();
    applyTransform(displayContext, poseStack, mesh.getExtent());
    mesh.render(
        poseStack,
        buffer.getBuffer(RealAppleMesh.renderType()),
        combinedLight,
        combinedOverlay);
    poseStack.popPose();
  }

  /** Only scale to fit Minecraft item space — GLB mesh/UV/texture are unchanged. */
  private static void applyTransform(ItemDisplayContext context, PoseStack pose, float extent) {
    float fit = 0.35F / extent;
    pose.translate(0.5F, 0.5F, 0.5F);
    switch (context) {
      case GUI:
        pose.mulPose(Axis.YP.rotationDegrees(30.0F));
        pose.mulPose(Axis.XP.rotationDegrees(15.0F));
        pose.scale(fit, fit, fit);
        break;
      case GROUND:
        pose.scale(fit, fit, fit);
        break;
      case FIXED:
        pose.scale(fit * 1.2F, fit * 1.2F, fit * 1.2F);
        break;
      case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND:
        pose.scale(fit, fit, fit);
        break;
      case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND:
        pose.scale(fit, fit, fit);
        break;
      default:
        pose.scale(fit, fit, fit);
        break;
    }
  }
}
