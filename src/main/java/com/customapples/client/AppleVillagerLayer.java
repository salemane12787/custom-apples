package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.customapples.util.AppleVillagerHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

public class AppleVillagerLayer extends RenderLayer<Villager, VillagerModel<Villager>> {
    private static final ResourceLocation APPLE_VILLAGER =
            CustomApplesMod.loc("textures/entity/apple_villager.png");

    public AppleVillagerLayer(RenderLayerParent<Villager, VillagerModel<Villager>> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack pose,
            MultiBufferSource buffer,
            int light,
            Villager villager,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        if (!villager.getTags().contains(AppleVillagerHelper.APPLE_VILLAGER_TAG)) {
            return;
        }
        getParentModel().renderToBuffer(
                pose,
                buffer.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(APPLE_VILLAGER)),
                light,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                1.0f,
                1.0f,
                1.0f,
                1.0f);
    }
}
