package com.customapples.client;

import com.customapples.entity.WormEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Blockbench worm model export. */
public class WormModel extends HierarchicalModel<WormEntity> {
    private final ModelPart bone;

    public WormModel(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        bone.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 4)
                        .addBox(0.06F, -1.0F, -0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 0).addBox(0.056F, -1.0F, 0.096F, 1.0F, 1.0F, 0.5F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.176F, 0.0F, 0.0F));

        bone.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(4, 4)
                        .addBox(-0.264F, -1.0F, 0.096F, 1.0F, 1.0F, 0.5F, new CubeDeformation(0.0F))
                        .texOffs(4, 2).addBox(-0.26F, -1.0F, -0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.12F, 0.0F, 0.0F));

        bone.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-0.04F, -1.0F, 0.096F, 1.0F, 1.0F, 0.5F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-0.036F, -1.0F, -0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.088F, 0.0F, 0.0F));

        bone.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(4, 6)
                        .addBox(-0.136F, -1.0F, 0.096F, 1.0F, 1.0F, 0.5F, new CubeDeformation(0.0F))
                        .texOffs(0, 2).addBox(-0.132F, -1.0F, -0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        bone.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(4, 0)
                        .addBox(0.028F, -1.0F, -0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 6).addBox(0.024F, -1.0F, 0.096F, 1.0F, 1.0F, 0.5F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.136F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return bone;
    }

    @Override
    public void setupAnim(WormEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        if (limbSwingAmount > 1.0E-5F) {
            entity.movingAnimationState.startIfStopped((int) ageInTicks);
        } else {
            entity.movingAnimationState.stop();
        }
        animate(entity.movingAnimationState, WormAnimations.MOVING, ageInTicks);
    }
}
