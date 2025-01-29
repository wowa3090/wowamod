// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

public class Modelmimic<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation("modid", "mimic"), "main");
	private final ModelPart osnova;
	private final ModelPart krishka;

	public Modelmimic(ModelPart root) {
		this.osnova = root.getChild("osnova");
		this.krishka = root.getChild("krishka");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition osnova = partdefinition.addOrReplaceChild("osnova", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-6.0F, 0.0F, -6.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = osnova.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).mirror()
				.addBox(-5.0F, 6.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(3, 3)
				.addBox(-5.0F, 6.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 6)
				.addBox(-5.0F, 6.0F, -5.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 6).mirror()
				.addBox(4.0F, 6.0F, -5.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(3, 3)
				.mirror().addBox(4.0F, 6.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 0).addBox(4.0F, 6.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition krishka = partdefinition.addOrReplaceChild("krishka", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r2 = krishka.addOrReplaceChild("cube_r2",
				CubeListBuilder.create().texOffs(6, 0).mirror()
						.addBox(4.0F, 9.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(6, 0).addBox(-5.0F, 9.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r3 = krishka.addOrReplaceChild("cube_r3",
				CubeListBuilder.create().texOffs(6, 6).mirror()
						.addBox(4.0F, 10.0F, -4.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(6, 6).addBox(-5.0F, 10.0F, -4.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

		PartDefinition cube_r4 = krishka.addOrReplaceChild("cube_r4",
				CubeListBuilder.create().texOffs(3, 9).mirror()
						.addBox(4.0F, 11.0F, -6.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(3, 9).addBox(-5.0F, 11.0F, -6.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition cube_r5 = krishka
				.addOrReplaceChild("cube_r5",
						CubeListBuilder.create().texOffs(0, 20).addBox(-6.0F, 9.0F, -11.0F, 12.0F, 2.0F, 12.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5672F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		osnova.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		krishka.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
	}
}