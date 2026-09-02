package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AppleBellRenderer implements BlockEntityRenderer<BellBlockEntity> {
    private static final ResourceLocation APPLE_BELL_BODY =
            CustomApplesMod.loc("entity/bell/bell_body");
    private static final Material APPLE_BELL_MATERIAL =
            new Material(BellRenderer.BELL_RESOURCE_LOCATION.atlasLocation(), APPLE_BELL_BODY);

    private final ModelPart bellBody;

    public AppleBellRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(ModelLayers.BELL);
        this.bellBody = root.getChild("bell_body");
    }

    @Override
    public void render(BellBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // Match vanilla BellRenderer — model pivot is already centered in the frame (8, 12, 8).
        float tick = (float) blockEntity.ticks + partialTick;
        float swingX = 0.0F;
        float swingZ = 0.0F;
        if (blockEntity.shaking) {
            float swing = Mth.sin(tick / (float) Math.PI) / (4.0F + tick / 3.0F);
            if (blockEntity.clickDirection == Direction.NORTH) {
                swingX = -swing;
            } else if (blockEntity.clickDirection == Direction.SOUTH) {
                swingX = swing;
            } else if (blockEntity.clickDirection == Direction.EAST) {
                swingZ = -swing;
            } else if (blockEntity.clickDirection == Direction.WEST) {
                swingZ = swing;
            }
        }

        this.bellBody.xRot = swingX;
        this.bellBody.zRot = swingZ;

        Material material = APPLE_BELL_MATERIAL;
        VertexConsumer vertexConsumer = material.buffer(buffer, RenderType::entitySolid);
        this.bellBody.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }
}
