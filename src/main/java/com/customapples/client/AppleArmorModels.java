package com.customapples.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

public class AppleArmorModels {
    public final ModelPart piece;
    public final ModelPart mat0;
    public final ModelPart mat1;
    public final ModelPart root;
    /** Blockbench helmet mesh — rendered on the player head, not the body root. */
    public final ModelPart helmetMesh;

    private AppleArmorModels(
            ModelPart piece,
            ModelPart mat0,
            ModelPart mat1,
            ModelPart root,
            ModelPart helmetMesh) {
        this.piece = piece;
        this.mat0 = mat0;
        this.mat1 = mat1;
        this.root = root;
        this.helmetMesh = helmetMesh;
    }

    public static AppleArmorModels single(ModelPart baked) {
        return new AppleArmorModels(baked.getChild("piece"), null, null, null, null);
    }

    public static AppleArmorModels blockbenchRoot(ModelPart baked) {
        ModelPart armor = baked.hasChild("root") ? baked.getChild("root") : baked;
        ModelPart m0 = armor.hasChild("mat0") ? armor.getChild("mat0") : armor;
        ModelPart m1 = armor.hasChild("mat1") ? armor.getChild("mat1") : null;
        ModelPart helmet = m0.hasChild("mesh_0") ? m0.getChild("mesh_0") : null;
        return new AppleArmorModels(null, m0, m1, armor, helmet);
    }

    public void renderPiece(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        if (piece != null) {
            piece.render(pose, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public void renderMat0(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        if (mat0 != null) {
            mat0.render(pose, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public void renderMat1(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        if (mat1 != null) {
            mat1.render(pose, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public void renderHelmetMesh(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        if (helmetMesh != null) {
            helmetMesh.render(pose, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
