package net.wowamod.client.model;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class Modelnightmare<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("universe3090", "modelnightmare"), "main");
	public final ModelPart head;
	public final ModelPart head2;

	public Modelnightmare(ModelPart root) {
		this.head = root.getChild("head");
		this.head2 = root.getChild("head2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition head = partdefinition.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(38, 0).addBox(-0.5263F, 4.2632F, -6.2237F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-0.5263F, 2.2632F, -9.2237F, 1.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-0.5263F, -5.7368F, -9.2237F, 1.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)).texOffs(8, 28).addBox(-0.5263F, -7.7368F, -6.9737F, 1.0F, 2.0F, 5.75F, new CubeDeformation(0.0F)).texOffs(0, 36)
						.addBox(-0.5263F, -3.7368F, -11.2237F, 1.0F, 6.0F, 22.0F, new CubeDeformation(0.0F)).texOffs(0, 36).mirror().addBox(-1.5263F, -3.7368F, -11.2237F, 1.0F, 6.0F, 22.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(38, 0)
						.mirror().addBox(-1.5263F, 4.2632F, -6.2237F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(8, 28).mirror().addBox(-1.5263F, -7.7368F, -6.9737F, 1.0F, 2.0F, 5.75F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(8, 28).addBox(-0.5263F, -7.7368F, 1.0263F, 1.0F, 2.0F, 5.75F, new CubeDeformation(0.0F)).texOffs(8, 28).mirror().addBox(-1.5263F, -7.7368F, 1.0263F, 1.0F, 2.0F, 5.75F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(0, 0).mirror().addBox(-1.5263F, 2.2632F, -9.2237F, 1.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(0, 0).mirror().addBox(-1.5263F, -5.7368F, -9.2237F, 1.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
						.mirror(false).texOffs(0, 39).addBox(-0.5263F, -5.7368F, 0.7763F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(0.4737F, -3.7368F, 2.7763F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-0.5263F, 0.2632F, 2.7763F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-0.5263F, 2.2632F, 4.7763F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-0.5263F, 2.2632F, 0.7763F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-0.5263F, 0.2632F, -5.2237F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-0.5263F, 2.2632F, -3.2237F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-0.5263F, 2.2632F, -7.2237F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(21, 0)
						.addBox(-0.5263F, -5.7368F, -7.2237F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.5263F, 3.7368F, 0.2237F));
		PartDefinition head2 = partdefinition.addOrReplaceChild("head2",
				CubeListBuilder.create().texOffs(58, 40).addBox(0.0F, 5.0F, 9.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(29, 41).addBox(0.0F, 7.0F, 7.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(26, 37)
						.addBox(0.0F, 11.0F, -9.0F, 1.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)).texOffs(56, 18).addBox(0.0F, 9.0F, 5.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(58, 25)
						.addBox(0.0F, 9.0F, -7.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(41, 20).addBox(0.0F, 7.0F, -11.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(58, 31)
						.addBox(0.0F, 5.0F, -11.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(58, 40).mirror().addBox(-1.0F, 5.0F, 9.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(29, 41).mirror()
						.addBox(-1.0F, 7.0F, 7.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(26, 37).mirror().addBox(-1.0F, 11.0F, -9.0F, 1.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(56, 18).mirror()
						.addBox(-1.0F, 9.0F, 5.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(58, 25).mirror().addBox(-1.0F, 9.0F, -7.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(41, 20).mirror()
						.addBox(-1.0F, 7.0F, -11.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(58, 31).mirror().addBox(-1.0F, 5.0F, -11.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(0.0F, 5.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
}
