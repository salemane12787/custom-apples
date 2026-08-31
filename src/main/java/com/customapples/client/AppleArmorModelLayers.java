package com.customapples.client;

import com.customapples.CustomApplesMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class AppleArmorModelLayers {
    public static final int TEX_W = 64;
    public static final int TEX_H = 32;
    /** Texel model is 16x vanilla density; scale down to player body units. */
    public static final float RENDER_SCALE = 1.0F / 16.0F;
    /** Chest HD sheet height — fit to player body. */
    public static final float CHEST_RENDER_SCALE = 12.0F / 32.0F;

    /** Scale Blockbench texel model onto the player body. */
    public static final float BLOCKBENCH_SCALE = 1.0F / 16.0F;

    /** Full Blockbench armor set — replace via blockbench/apple_armor.bbmodel + import script. */
    public static final ModelLayerLocation APPLE_ARMOR_SET =
            new ModelLayerLocation(CustomApplesMod.loc("apple_armor_set"), "main");

    private AppleArmorModelLayers() {
    }

    public static LayerDefinition appleArmorSet() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition armor = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(17.0F, 43.0F, -1.0F));
        PartDefinition mat0 = armor.addOrReplaceChild("mat0", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition mat1 = armor.addOrReplaceChild("mat1", CubeListBuilder.create(), PartPose.ZERO);
        mat0.addOrReplaceChild(
                "mesh_0",
                CubeListBuilder.create().texOffs(16, 16).addBox(-6.0F, -4.0F, -4.0F, 10.0F, 14.0F, 6.0F),
                PartPose.offset(-16.0F, -28.0F, 2.0F));
        mat0.addOrReplaceChild(
                "mesh_1",
                CubeListBuilder.create().texOffs(40, 16).addBox(0.0F, -3.0F, -5.0F, 6.0F, 14.0F, 6.0F),
                PartPose.offset(-26.0F, -29.0F, 3.0F));
        mat0.addOrReplaceChild(
                "mesh_2",
                CubeListBuilder.create().texOffs(40, 16).addBox(-5.0F, -5.0F, -5.0F, 6.0F, 14.0F, 6.0F),
                PartPose.offset(-9.0F, -27.0F, 3.0F));
        mat0.addOrReplaceChild(
                "mesh_3",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -4.0F, -4.0F, 10.0F, 10.0F, 10.0F),
                PartPose.offset(-16.0F, -16.0F, 0.0F));
        mat0.addOrReplaceChild(
                "mesh_4",
                CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 0.0F, -5.0F, 6.0F, 14.0F, 6.0F),
                PartPose.offset(-19.0F, -44.0F, 3.0F));
        mat0.addOrReplaceChild(
                "mesh_5",
                CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 0.0F, -5.0F, 6.0F, 14.0F, 6.0F),
                PartPose.offset(-15.0F, -44.0F, 3.0F));
        mat1.addOrReplaceChild(
                "mesh_6",
                CubeListBuilder.create().texOffs(16, 23).addBox(-0.6000000238418579F, -6.599999904632568F, -0.6000000238418579F, 9.200000405311584F, 6.199999898672104F, 5.199999928474426F),
                PartPose.offset(-21.0F, -26.0F, -1.0F));
        mat1.addOrReplaceChild(
                "mesh_7",
                CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -2.5F, -0.5F, 5.0F, 10.0F, 5.0F),
                PartPose.offset(-17.0F, -38.0F, -1.0F));
        mat1.addOrReplaceChild(
                "mesh_8",
                CubeListBuilder.create().texOffs(0, 16).addBox(-4.5F, -2.5F, -0.5F, 5.0F, 10.0F, 5.0F),
                PartPose.offset(-17.0F, -38.0F, -1.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }
    public static LayerDefinition appleChestplate() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Full 2048x1024 artwork on the chest front (no downscale in texture file).
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1024.0F, -512.0F, -8.0F, 2048.0F, 1024.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(1280, 512)
                        .addBox(-64.0F, -24.0F, -64.0F, 128.0F, 384.0F, 128.0F, new CubeDeformation(64.0F)),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(1280, 512)
                        .addBox(-64.0F, -24.0F, -64.0F, 128.0F, 384.0F, 128.0F, new CubeDeformation(64.0F)),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static LayerDefinition appleLeggings() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "left",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-16.0F, 0.0F, -16.0F, 32.0F, 96.0F, 32.0F, new CubeDeformation(2.0F)),
                PartPose.offset(16.0F, 0.0F, 0.0F));
        root.addOrReplaceChild(
                "right",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-16.0F, 0.0F, -16.0F, 32.0F, 96.0F, 32.0F, new CubeDeformation(2.0F)),
                PartPose.offset(-16.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static LayerDefinition appleBoots() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "left",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-16.0F, 48.0F, -16.0F, 32.0F, 48.0F, 32.0F, new CubeDeformation(3.0F)),
                PartPose.offset(16.0F, 0.0F, 0.0F));
        root.addOrReplaceChild(
                "right",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-16.0F, 48.0F, -16.0F, 32.0F, 48.0F, 32.0F, new CubeDeformation(3.0F)),
                PartPose.offset(-16.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }
}
