package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.customapples.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/** Renders the Blockbench GLTF armor model with layer_1 + layer_2 HD textures. */
public class AppleArmorLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation LAYER1 =
            CustomApplesMod.loc("textures/models/armor/hd/apple_layer_1.png");
    private static final ResourceLocation LAYER2 =
            CustomApplesMod.loc("textures/models/armor/hd/apple_layer_2.png");

    private final AppleArmorModels armorModel;

    public AppleArmorLayer(PlayerRenderer renderer, EntityModelSet models) {
        super(renderer);
        this.armorModel = AppleArmorModels.blockbenchRoot(bake(models, AppleArmorModelLayers.APPLE_ARMOR_SET));
    }

    private static ModelPart bake(EntityModelSet models, ModelLayerLocation layer) {
        return models.bakeLayer(layer);
    }

    @Override
    public void render(
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        if (player.isInvisible()) {
            return;
        }
        PlayerModel<AbstractClientPlayer> parent = getParentModel();
        boolean headArmor = showsAppleHelmet(player);

        if (wearsAppleArmor(player)) {
            renderArmorSet(pose, buffer, packedLight, parent, headArmor);
        }
        if (headArmor) {
            renderAppleHelmetOnHead(pose, buffer, packedLight, parent);
        }
    }

    /** Iron apple helmet + apple chestplate both use mesh_0 from the full armor UV sheet. */
    private static boolean showsAppleHelmet(AbstractClientPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.IRON_APPLE.get())
                || player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.APPLE_CHESTPLATE.get());
    }

    private static boolean wearsAppleArmor(AbstractClientPlayer player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.APPLE_CHESTPLATE.get())
                || player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.APPLE_LEGGINGS.get())
                || player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.APPLE_BOOTS.get());
    }

    private void renderArmorSet(
            PoseStack pose,
            MultiBufferSource buffer,
            int light,
            PlayerModel<AbstractClientPlayer> parent,
            boolean hideHelmet) {
        pose.pushPose();
        parent.body.translateAndRotate(pose);
        pose.scale(AppleArmorModelLayers.BLOCKBENCH_SCALE, AppleArmorModelLayers.BLOCKBENCH_SCALE,
                AppleArmorModelLayers.BLOCKBENCH_SCALE);
        if (hideHelmet && armorModel.helmetMesh != null) {
            armorModel.helmetMesh.visible = false;
        }
        var layer1 = buffer.getBuffer(RenderType.entityCutoutNoCull(LAYER1));
        armorModel.renderMat0(pose, layer1, light, OverlayTexture.NO_OVERLAY);
        if (hideHelmet && armorModel.helmetMesh != null) {
            armorModel.helmetMesh.visible = true;
        }
        if (armorModel.mat1 != null) {
            var layer2 = buffer.getBuffer(RenderType.entityCutoutNoCull(LAYER2));
            armorModel.renderMat1(pose, layer2, light, OverlayTexture.NO_OVERLAY);
        }
        pose.popPose();
    }

    /** Helmet UV is on layer_1 — same mesh + texture as the full armor set. */
    private void renderAppleHelmetOnHead(
            PoseStack pose,
            MultiBufferSource buffer,
            int light,
            PlayerModel<AbstractClientPlayer> parent) {
        if (armorModel.helmetMesh == null) {
            return;
        }
        pose.pushPose();
        parent.head.translateAndRotate(pose);
        pose.scale(AppleArmorModelLayers.RENDER_SCALE, AppleArmorModelLayers.RENDER_SCALE,
                AppleArmorModelLayers.RENDER_SCALE);
        pose.translate(16.0F, 28.0F, -2.0F);
        var consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(LAYER1));
        armorModel.renderHelmetMesh(pose, consumer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
