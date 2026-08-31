package com.customapples.client;

import com.customapples.entity.WormEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WormRenderer extends MobRenderer<WormEntity, WormModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("customapples", "textures/entity/worm.png");

    public WormRenderer(EntityRendererProvider.Context context) {
        super(context, new WormModel(context.bakeLayer(WormModelLayers.WORM)), 0.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(WormEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(WormEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        // Blockbench model length is along X; Minecraft forward is Z.
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
    }

    @Override
    protected void scale(WormEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.0F, 1.0F, 1.0F);
    }
}
